package sprest.user;

import sprest.data.QueryManager;
import sprest.exception.DuplicateUserException;
import sprest.exception.InavlidOrExpiredPasswordResetTokenException;
import sprest.exception.NotFoundByUniqueKeyException;
import sprest.user.dtos.PasswordResetRequest;
import sprest.user.dtos.UserDto;
import sprest.user.repositories.UserAuthorityRepository;
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

import static sprest.user.UserRight.values.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagerTest {

    public static final String TEST_EMAIL = "test@somewhere.de";

    @Mock
    UserRepository userRepository;
    @Mock
    UserAuthorityRepository userAuthorityRepository;
    RandomStringManager randomStringManager = new RandomStringManager();
    @Mock
    QueryManager<AppUser> queryManager;
    @Mock
    EmailSender emailSender;

    UserManager userManager;

    @BeforeEach
    public void setup() {
        userManager = new UserManager(userRepository,
            randomStringManager, queryManager,
            emailSender, 20, userAuthorityRepository);
    }

    @Test
    public void mustThrowExceptionWhenInvalidUserId() {
        try {
            userManager.sendResetPasswordLink(12345);
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
            userManager.sendResetPasswordLink(r);
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
        userManager.sendResetPasswordLink(r);

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
            userManager.resetPassword("abc");
            fail("Should throw an exception!");
        } catch (InavlidOrExpiredPasswordResetTokenException e) {
            // invalid token
        }
        try {
            userManager.resetPassword("abc");
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
            var password = userManager.resetPassword("abc");
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
    public void mustReturnGrantableUserRightsForSuperAdmin() {
        // given
        var user = new AppUser();
        var setupClientsRight = new UserAuthority();
        setupClientsRight.setAuthority(MANAGE_ANNOUNCEMENTS);
        user.setRights(Set.of(setupClientsRight));
        var allAuthorities = getMockListOfAuthorities(
            List.of("MANAGE_1", "MANAGE_2", "INVOKE_1", "INVOKE_2", "ACCESS_1", MANAGE_ANNOUNCEMENTS));

        // when
        when(userAuthorityRepository.findAll()).thenReturn(allAuthorities);
        var grantableRights = (List<UserAuthority>) userManager.getGrantableRights(user);

        // expect
        assertEquals(allAuthorities.size(), grantableRights.size());
        for (var auth : allAuthorities) {
            assertTrue(grantableRights.contains(auth));
        }
    }

    @Test
    public void mustReturnGrantableUserRightsForAdminWithManageAll() {
        // given
        var user = new AppUser();
        var manageAllRight = new UserAuthority();
        manageAllRight.setAuthority(MANAGE_ALL);
        user.setRights(Set.of(manageAllRight));
        var allAuthorities = getMockListOfAuthorities(
            List.of("MANAGE_1", "MANAGE_2", "INVOKE_1", "INVOKE_2", "ACCESS_1", MANAGE_ANNOUNCEMENTS));
        var subscribedAuthorities = allAuthorities.stream().filter(
            auth -> auth.getAuthority().endsWith("_1")).toList();

        // when
        when(userAuthorityRepository.findAll()).thenReturn(allAuthorities);
        var grantableRights = (List<UserAuthority>) userManager.getGrantableRights(user);

        // expect
        assertEquals(subscribedAuthorities.size(), grantableRights.size());
        for (var auth : subscribedAuthorities) {
            assertTrue(grantableRights.contains(auth));
        }
    }

    @Test
    public void mustReturnGrantableUserRightsForAdminWithPartialManageRights() {
        // given
        var user = new AppUser();
        var manageUsersRight = new UserAuthority();
        manageUsersRight.setAuthority(MANAGE_USERS);
        user.setRights(Set.of(manageUsersRight));
        var allAuthorities = getMockListOfAuthorities(
            List.of(MANAGE_USERS, "MANAGE_TEST"));

        // when
        when(userAuthorityRepository.findAll()).thenReturn(allAuthorities);
        var grantableRights = (List<UserAuthority>) userManager.getGrantableRights(user);

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
        assertFalse(userManager.toggleUserIsActive(activeUserId, false).isActive());
        assertTrue(userManager.toggleUserIsActive(activeUserId, true).isActive());
        assertFalse(userManager.toggleUserIsActive(inactiveUserId, false).isActive());
        assertTrue(userManager.toggleUserIsActive(inactiveUserId, true).isActive());
    }

    @Test
    public void mustCreateUser() {
        // given
        var admin = new AppUser();
        var adminRight = new UserAuthority();
        adminRight.setAuthority(MANAGE_USERS);
        adminRight.setId(1);
        admin.setRights(Set.of(adminRight));

        var dto = new UserDto();
        dto.setUserName("user");
        dto.setRights(Set.of(adminRight));

        var user = new AppUser();
        user.setId(1);

        var invalidAdmin = new AppUser();
        invalidAdmin.setRights(Set.of());

        var invalidDto = new UserDto();
        invalidDto.setUserName("user");
        invalidDto.setRights(Set.of(new UserAuthority()));

        // when
        when(userAuthorityRepository.findAll()).thenReturn(List.of(adminRight));
        when(userRepository.save(any(AppUser.class))).thenReturn(user);

        // expect
        assertNotNull(userManager.createUser(dto, admin).getId());
        assertThrows(AccessDeniedException.class, () -> userManager.createUser(dto, invalidAdmin));
        assertThrows(ResponseStatusException.class, () -> userManager.createUser(invalidDto, admin));
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
        assertThrows(DuplicateUserException.class, () -> userManager.createUser(emailDto, new AppUser()));
        assertThrows(DuplicateUserException.class, () -> userManager.createUser(userNameDto, new AppUser()));
    }

    private List<UserAuthority> getMockListOfAuthorities(List<String> authNames) {
        return authNames.stream().map(authName -> {
            var auth = new UserAuthority();
            auth.setAuthority(authName);
            return auth;
        }).toList();
    }
}