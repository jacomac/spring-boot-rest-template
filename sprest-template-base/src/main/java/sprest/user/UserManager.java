package sprest.user;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import sprest.data.QueryManager;
import sprest.exception.DuplicateUserException;
import sprest.exception.InavlidOrExpiredPasswordResetTokenException;
import sprest.exception.NotFoundByUniqueKeyException;
import sprest.user.dtos.PasswordResetRequest;
import sprest.user.dtos.UserDto;
import sprest.user.dtos.UserSelfAdminDto;
import sprest.user.repositories.AccessRightRepository;
import sprest.user.repositories.UserRepository;
import sprest.user.services.RandomStringManager;
import sprest.utils.DateUtils;
import sprest.utils.EmailSender;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.StreamSupport;

import static sprest.user.UserRight.values.MANAGE_ALL;
import static sprest.user.UserRight.values.MANAGE_ANNOUNCEMENTS;

@Slf4j
@Component
@Transactional
public class UserManager {

    private final int defaultPasswordLength;
    private final UserRepository userRepository;
    private final AccessRightRepository accessRightRepository;
    private final RandomStringManager randomStringManager;
    private final QueryManager<AppUser> queryManager;
    private final EmailSender emailSender;

    public UserManager(UserRepository userRepository,
                       RandomStringManager randomStringManager,
                       QueryManager<AppUser> queryManager, EmailSender emailSender,
                       @Value("${user.passwordLength:12}") int passwordLength,
                       AccessRightRepository accessRightRepository
    ) {
        this.userRepository = userRepository;
        this.randomStringManager = randomStringManager;
        this.queryManager = queryManager;
        this.emailSender = emailSender;
        this.defaultPasswordLength = passwordLength;
        this.accessRightRepository = accessRightRepository;
    }

    // make sure user authorities in DB are the same as in the right enums
    @PostConstruct
    private void checkUserAuthorities() {
        List<String> mismatchingRights = new ArrayList<>();
        var authorities = accessRightRepository.findAll();
        List<String> authoritiesAsList = StreamSupport
            .stream(authorities.spliterator(), false)
            .map(AccessRight::getName).toList();
        var rightEnumValues = AllUserRights.getInstance().getValues();

        // coming from DB
        for (AccessRight auth : authorities) {
            if (!rightEnumValues.contains(auth.getName())) {
                mismatchingRights.add(auth.getName());
            }
        }
        // coming from enum
        for (String right : rightEnumValues) {
            if (!authoritiesAsList.contains(right)) {
                mismatchingRights.add(right);
            }
        }

        log.warn("Mismatching rights found: {}", mismatchingRights);
    }


    // search by multiple criteria
    public Page<AppUser> getFilteredUsers(UserSearchFilter filter, Pageable page) {
        return queryManager.findByMultiSearch(filter, page, AppUser.class);
    }

    public Page<AppUser> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public AppUser getById(int id) {
        return userRepository.findById(id).orElseThrow(
            () -> new NotFoundByUniqueKeyException(String.format("Anwender mit ID %d konnte nicht" +
                " gefunden werden.", id)));
    }

    public AppUser getByUserName(String userName) {
        return userRepository.findByUserName(userName).orElseThrow(
            () -> new NotFoundByUniqueKeyException(String.format(
                "Anwender mit login %s konnte nicht gefunden werden.",
                userName)));
    }

    public AppUser toggleUserIsActive(int id, boolean active) {
        var user = getById(id);
        user.setActive(active);

        return userRepository.save(user);
    }

    public synchronized AppUser createUser(UserDto dto, AppUser principal) {
        checkUniqueConstraints(dto);
        checkRightsAssignment(dto, principal);

        var user = new AppUser();
        user.copyDto(dto);
        String generatedPassword =
            randomStringManager.generateRandomAlphanumericString(defaultPasswordLength);
        user.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(generatedPassword));
        var userAuthorities = user.getAccessRights().toArray(AccessRight[]::new);

        return userRepository.save(user);
    }

    private void checkUniqueConstraints(UserDto dto) {
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new DuplicateUserException(String.format("Die Email %s wird bereits verwendet",
                dto.getEmail()));
        }
        assert dto.getUserName() != null;
        if (userRepository.existsByUserNameIgnoreCase(dto.getUserName())) {
            throw new DuplicateUserException(String.format("Der Anmeldename %s wird bereits " +
                "verwendet", dto.getUserName()));
        }
    }

    public AppUser getUserByPrincipal(Principal auth) {
        var principal = ((UsernamePasswordAuthenticationToken) auth).getPrincipal();
        AppUser user = ((UserPrincipal) principal).getUser();
        assert user != null;
        return user;
    }

    /**
     * Method dedicated for admin users to reset individual user password.
     *
     * @param userId user identifier
     */
    public void sendResetPasswordLink(int userId) {
        try {
            AppUser user = getById(userId);
            doSendResetPasswordLink(user);
        } catch (NotFoundByUniqueKeyException e) {
            log.warn("Can't reset password for user with id '{}', because it doesn't exist.",
                userId);
            throw e;
        }
    }

    public void sendResetPasswordLink(PasswordResetRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElse(null);

        if (user == null) {
            log.warn("Can't reset password for user with email '{}', because it doesn't exist.",
                request.getEmail());
            return;
        }
        doSendResetPasswordLink(user);
    }

    private void doSendResetPasswordLink(AppUser user) {
        var token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenValidUntil(DateUtils.convert(LocalDate.now().plusDays(1)));
        userRepository.save(user);
        emailSender.sendPasswordResetEmail(user);
    }

    public String resetPassword(String token)
        throws InavlidOrExpiredPasswordResetTokenException {
        log.info("Resetting password using token {}.", token);
        var user = userRepository.findByPasswordResetToken(token).orElse(null);

        if (user == null || user.getPasswordResetTokenValidUntil().before(new Date())) {
            throw new InavlidOrExpiredPasswordResetTokenException("Provided token is invalid!");
        }

        return doResetPassword(user);
    }

    public String resetPassword(int userId) {
        var user = getById(userId);

        return doResetPassword(user);
    }

    private String doResetPassword(AppUser user) {
        String generatedPassword =
            randomStringManager.generateRandomAlphanumericString(defaultPasswordLength);
        user.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(generatedPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenValidUntil(null);
        userRepository.save(user);

        return generatedPassword;
    }

    public UserDtoWithId updateUser(int id, UserDto dto, AppUser principal) {
        AppUser user = userRepository.findById(id).orElseThrow();
        checkIfEmailNotDuplicated(user.getEmail(), dto.getEmail());
        checkIfUserNameNotChanged(user.getUserName(), dto.getUserName());

        if (Arrays.deepEquals(dto.getAccessRights().toArray(), user.getAccessRights().toArray())) {
            checkRightsAssignment(dto, principal);
        }

        user.copyDto(dto);

        return userRepository.save(user).toUserDtoWithId();
    }

    private void checkIfEmailNotDuplicated(String email, String dtoEmail) {
        if (email != null && !email.equalsIgnoreCase(dtoEmail)) {
            if (userRepository.existsByEmailIgnoreCase(dtoEmail)) {
                throw new DuplicateUserException(String.format("Die Email %s wird bereits " +
                    "verwendet", dtoEmail));
            }
        }
    }

    private void checkIfUserNameNotChanged(String userName, String dtoUserName) {
        if (!userName.equalsIgnoreCase(dtoUserName)) {
            throw new DuplicateUserException("Ein einmal vergebender Anmeldename darf nicht " +
                "geändert werden");
        }
    }

    public UserDtoWithId changeSelfUserData(UserSelfAdminDto user, AppUser principal) {
        var existingUser = getById(principal.getId());
        existingUser.copySelfAdminDto(user);

        return userRepository.save(existingUser).toUserDtoWithId();
    }

    private void checkRightsAssignment(UserDto dto, AppUser user) {
        Iterable<AccessRight> grantable = getGrantableRights(user);

        for (AccessRight authority : dto.getAccessRights()) {
            if (authority.getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Authority must contains an id");
            }
            var right = authority.getName();
            if (right.startsWith("MANAGE_") || right.startsWith("SETUP_")) {
                boolean isAllowed = false;
                for (AccessRight g : grantable) {
                    if (g.getName().equalsIgnoreCase(right)) {
                        isAllowed = true;
                        break;
                    }
                }
                if (!isAllowed) {
                    throw new AccessDeniedException(String
                        .format(
                            "Sie dürfen das Recht %s nicht vergeben, da Sie es selbst nicht " +
                                "besitzen",
                            right));
                }
            }
        }
    }

    public Iterable<AccessRight> getGrantableRights(AppUser user) {
        var authorities = (List<AccessRight>) accessRightRepository.findAll(); //enforce to
        // only accept ENUM values

        if (user.hasRight(MANAGE_ANNOUNCEMENTS)) {
            return authorities;
        }
        return authorities.stream()
            .filter(
                authority -> {
                    var authName = authority.getName();
                    return canGrantAuthority(user, authName);
                }
            )
            .sorted(Comparator.comparing(AccessRight::getName))
            .toList();
    }

    private boolean canGrantAuthority(AppUser user, String authName) {
        String managePrefix = "MANAGE_";
        String setupPrefix = "SETUP_";

        boolean isManageRight = authName.startsWith(managePrefix);
        boolean canGrantManageRight = (user.hasRight(authName) || user.hasRight(MANAGE_ALL));
        boolean isNotManageOrSetupRight =
            !(authName.startsWith(managePrefix) || authName.startsWith(setupPrefix));

        return (isManageRight && canGrantManageRight) || isNotManageOrSetupRight;
    }

    public Iterable<String> getAvailableRights() {
        return AllUserRights.getInstance().getValues();
    }

    public Boolean existsByClientIdAndUserName(Integer clientId, String username) {
        return userRepository.existsByUserNameIgnoreCase(username.toLowerCase());
    }

    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

}
