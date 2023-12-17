package sprest.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import sprest.data.QueryManager;
import sprest.exception.DuplicateUserException;
import sprest.exception.InavlidOrExpiredPasswordResetTokenException;
import sprest.exception.NotFoundByUniqueKeyException;
import sprest.user.dtos.PasswordResetRequest;
import sprest.user.dtos.UserDto;
import sprest.user.repositories.AccessRightRepository;
import sprest.user.repositories.UserRepository;
import sprest.user.services.RandomStringManager;
import sprest.utils.DateUtils;
import sprest.utils.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static sprest.user.BaseRight.values.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    public static final String TEST_EMAIL = "test@somewhere.de";

    @Mock
    UserRepository userRepository;
    @Mock
    AccessRightRepository accessRightRepository;
    RandomStringManager randomStringManager = new RandomStringManager();
    @Mock
    QueryManager<AppUser> queryManager;
    @Mock
    EmailSender emailSender;
    @Autowired
    Environment environment;

    UserService userService;

    @BeforeEach
    public void setup() {
        userService = new UserService(userRepository,
            randomStringManager, queryManager,
            emailSender, accessRightRepository, "",
            environment);
    }

    @Test
    public void mustThrowExceptionWhenInvalidUserId() {
        try {
            userService.sendResetPasswordLink(12345);
            fail("Should throw exception!");
        } catch (NotFoundByUniqueKeyException e) {
            // no op
        }
    }

    @Test
    public void mustNotThrowExceptionWhenInvalidEmail() {
        var r = new PasswordResetRequest();
        r.setEmail("not-existing@mail.com");
        try {
            userService.sendResetPasswordLink(r);
        } catch (Exception e) {
            fail("Should not throw exception!");
        }
    }

    @Test
    public void mustSendEmailWithPasswordResetLink() {
        // given
        var u = new AppUser();
        u.setEmail(TEST_EMAIL);
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(u));
        var r = new PasswordResetRequest();
        r.setEmail(TEST_EMAIL);

        //when
        userService.sendResetPasswordLink(r);

        //then
        verify(emailSender, times(1)).sendPasswordResetEmail(u);
    }

    @Test
    public void mustThrowAnExceptionWhenTokenIsInvalidOrExpired() {
        // given
        var userWithExpiredToken = new AppUser();
        userWithExpiredToken.setPasswordResetToken("abc");
        userWithExpiredToken.setPasswordResetTokenValidUntil(DateUtils.convert(LocalDate.now().minusDays(1)));
        when(userRepository.findByPasswordResetToken("abc")).thenReturn(Optional.empty(), Optional.of(userWithExpiredToken));
        // expect
        try {
            userService.resetPassword("abc");
            fail("Should throw an exception!");
        } catch (InavlidOrExpiredPasswordResetTokenException e) {
            // invalid token
        }
        try {
            userService.resetPassword("abc");
            fail("Should throw an exception!");
        } catch (InavlidOrExpiredPasswordResetTokenException e) {
            // expired token
        }
    }

    @Test
    public void mustSetNewUserPassword() {
        // given
        var user = new AppUser();
        user.setPasswordResetToken("abc");
        user.setPasswordResetTokenValidUntil(DateUtils.convert(LocalDate.now().plusDays(1)));
        when(userRepository.findByPasswordResetToken("abc")).thenReturn(Optional.of(user));

        // expect
        try {
            var password = userService.resetPassword("abc");
            assertNotNull(password);
            verify(userRepository, times(1)).save(user);
            assertNotNull(user.getPassword());
            assertNull(user.getPasswordResetToken());
            assertNull(user.getPasswordResetTokenValidUntil());
        } catch (InavlidOrExpiredPasswordResetTokenException e) {
            fail("Shouldn't throw an exception!");
        }
    }

    @Test
    public void mustReturnGrantableUserRightsForAdminWithPartialManageRights() {
        // given
        var user = new AppUser();
        var manageUsersRight = new AccessRight();
        manageUsersRight.setName(MANAGE_USERS);
        user.setAccessRights(Set.of(manageUsersRight));
        var allAuthorities = getMockListOfAuthorities(
            List.of(MANAGE_USERS, "MANAGE_TEST"));

        // when
        when(accessRightRepository.findAll()).thenReturn(allAuthorities);
        var grantableRights = (List<AccessRight>) userService.getGrantableRights(user);

        // expect
        assertEquals(1, grantableRights.size());
        assertTrue(grantableRights.contains(manageUsersRight));
    }

    @Test
    public void mustToggleUserActive() {
        // given
        int activeUserId = 99;
        int inactiveUserId = 100;
        var activeUser = new AppUser();
        activeUser.setActive(true);
        activeUser.setId(activeUserId);
        var inactiveUser = new AppUser();
        inactiveUser.setActive(false);
        inactiveUser.setId(inactiveUserId);

        // when
        when(userRepository.findById(activeUserId)).thenReturn(Optional.of(activeUser));
        when(userRepository.findById(inactiveUserId)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(activeUser)).thenReturn(activeUser);
        when(userRepository.save(inactiveUser)).thenReturn(inactiveUser);

        // expect
        assertFalse(userService.toggleUserIsActive(activeUserId, false).isActive());
        assertTrue(userService.toggleUserIsActive(activeUserId, true).isActive());
        assertFalse(userService.toggleUserIsActive(inactiveUserId, false).isActive());
        assertTrue(userService.toggleUserIsActive(inactiveUserId, true).isActive());
    }

    @Test
    public void mustCreateUser() {
        // given
        var admin = new AppUser();
        var adminRight = new AccessRight();
        adminRight.setName(MANAGE_USERS);
        adminRight.setId(1);
        admin.setAccessRights(Set.of(adminRight));

        var dto = new UserDto();
        dto.setUserName("user");
        dto.setAccessRights(Set.of(adminRight));

        var user = new AppUser();
        user.setId(1);

        var invalidAdmin = new AppUser();
        invalidAdmin.setAccessRights(Set.of());

        var invalidDto = new UserDto();
        invalidDto.setUserName("user");
        invalidDto.setAccessRights(Set.of(new AccessRight()));

        // when
        when(accessRightRepository.findAll()).thenReturn(List.of(adminRight));
        when(userRepository.save(any(AppUser.class))).thenReturn(user);

        // expect
        assertNotNull(userService.createUser(dto, admin).getId());
        assertThrows(AccessDeniedException.class, () -> userService.createUser(dto, invalidAdmin));
        assertThrows(ResponseStatusException.class, () -> userService.createUser(invalidDto, admin));
    }

    @Test
    public void mustFailUniqueConstraintsCheck() {
        // given
        var emailDto = new UserDto();
        emailDto.setEmail("email@mail.com");

        var userName = "user";
        var userNameDto = new UserDto();
        userNameDto.setUserName(userName);

        // when
        when(userRepository.existsByEmailIgnoreCase(emailDto.getEmail())).thenReturn(true);
        when(userRepository.existsByUserNameIgnoreCase(userName)).thenReturn(true);

        // expect
        assertThrows(DuplicateUserException.class, () -> userService.createUser(emailDto, new AppUser()));
        assertThrows(DuplicateUserException.class, () -> userService.createUser(userNameDto, new AppUser()));
    }

    private List<AccessRight> getMockListOfAuthorities(List<String> authNames) {
        return authNames.stream().map(authName -> {
            var auth = new AccessRight();
            auth.setName(authName);
            return auth;
        }).toList();
    }
}