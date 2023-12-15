package sprest.admin;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import sprest.ControllerTestBase;
import sprest.user.AppUser;
import sprest.user.UserAuthority;
import sprest.user.UserPrincipal;
import sprest.user.UserRight;
import sprest.user.dtos.UserDto;
import sprest.user.repositories.UserAuthorityRepository;
import sprest.user.repositories.UserRepository;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sprest.user.UserRight.values.*;

/**
 * @author Konrad Wulf
 */
class UserAdminControllerTest extends ControllerTestBase {

    private final String BASE_PATH = "/admin/users";

    @Autowired
    UserAuthorityRepository userAuthorityRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    public void shouldGetUserById() throws Exception {
        var user = getMockUser(MANAGE_USERS);
        var rights = userAuthorityRepository.saveAll(user.getRights());
        user.setPassword("password");
        Set<UserAuthority> rightsSet = new HashSet<>();
        rights.forEach(rightsSet::add);
        user.setRights(rightsSet);
        var savedUser = userRepository.save(user);

        mockMvc
            .perform(get(BASE_PATH + "/" + savedUser.getId())
                .with(user(new UserPrincipal(user))))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void shouldStoreUserNonPractitionerUserWrongRights() throws Exception {
        var user = getMockUser(ACCESS_ALL);

        try {
            tryToStoreNonPractitionerUser(user);
            fail(
                "should have not been possible to manipulate user without the correct admin rights");
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("403")); // http status should be forbidden
        }
    }

    @Test
    public void shouldBeAbleToAssignAllManageRights() throws Exception {
        var clientAdmin = getMockUser(MANAGE_ALL);

        mockMvc
            .perform(get(BASE_PATH + "/rights/grantable").with(
                user(new UserPrincipal(clientAdmin))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$..*", Matchers.hasItem(UserRight.MANAGE_USERS.toString())));
    }

    @Test
    public void shouldStoreUserNonPractitionerUserManageAllRight() throws Exception {
        var user = getMockUser(MANAGE_ALL);
        tryToStoreNonPractitionerUser(user);
    }

    @Test
    public void shouldListAllAvailableRights() throws Exception {
        var user = getMockUser(MANAGE_USERS);
        mockMvc
            .perform(
                get(BASE_PATH + "/rights/grantable")
                    .with(user(new UserPrincipal(user))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$..*", Matchers.hasItem(ACCESS_ALL)));
    }

    private void tryToStoreNonPractitionerUser(AppUser user) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(
            getClass().getClassLoader().getResource("sprest/admin/TestUserCreate.json").toURI())));

        mockMvc
            .perform(
                post(BASE_PATH)
                    .content(json)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf())
                    .with(user(new UserPrincipal(user))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", greaterThan(0)));
    }


    @Test
    public void shouldReturnGrantableRights() throws Exception {
        // FAILURE: principal has not set up right => not assignable
        var adminOfCustomerX = getMockUser(MANAGE_USERS);

        mockMvc
            .perform(get(BASE_PATH + "/rights/grantable").with(
                user(new UserPrincipal(adminOfCustomerX))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$..*", Matchers.hasItem(MANAGE_USERS)))
            .andExpect(jsonPath("$..*",
                CoreMatchers.not(Matchers.hasItem(MANAGE_ANNOUNCEMENTS))));
    }

    @Test
    public void shouldAssignRights() throws Exception {
        var adminOfCustomerX = getMockUser(MANAGE_USERS);

        var nu = new UserDto();
        nu.setFirstName("firstName123");
        nu.setLastName("lastName123");
        nu.setUserName("admin5");
        nu.setEmail("aa@test.de");
        var auAuthorities = new HashSet<UserAuthority>();

        var manageUsers = userAuthorityRepository.findByAuthority("MANAGE_USERS");
        manageUsers.ifPresent(auAuthorities::add);

        nu.setRights(auAuthorities);

        var nuJson = objectMapper.writeValueAsString(nu);
        MvcResult apiResponse = mockMvc
            .perform(
                post(BASE_PATH)
                    .content(nuJson)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user(new UserPrincipal(adminOfCustomerX)))
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        AppUser user = objectMapper.readValue(apiResponse.getResponse().getContentAsString(),
            AppUser.class);
        assertTrue(user.getId() > 0);

        var userAuthority = new UserAuthority();
        userAuthority.setId(1);
        userAuthority.setAuthority("MANAGE_ANNOUNCEMENTS");

        var userRights = user.getRights();
        userRights.add(userAuthority);

        user.setRights(userRights);

        var request = objectMapper.writeValueAsString(user);
        mockMvc
            .perform(
                put(BASE_PATH + "/{id}", user.getId())
                    .content(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user(new UserPrincipal(adminOfCustomerX)))
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest());

    }

    @Test
    public void shouldValidateSuperAdminCapabilities() throws Exception {
        var superAdmin = getMockUser(MANAGE_ANNOUNCEMENTS, MANAGE_ALL);

        mockMvc
            .perform(
                get(BASE_PATH + "/rights/grantable").with(user(new UserPrincipal(superAdmin))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$..*", Matchers.hasItem(MANAGE_ANNOUNCEMENTS)))
            .andReturn();

        var sa = new UserDto();
        sa.setFirstName("firstName123");
        sa.setLastName("lastName123");
        sa.setUserName("admin4");
        sa.setEmail("aa@test.de");
        var manageUsers = userAuthorityRepository.findByAuthority("MANAGE_USERS");
        var setupClient = userAuthorityRepository.findByAuthority("MANAGE_ANNOUNCEMENTS");

        var authorities = new HashSet<UserAuthority>();
        manageUsers.ifPresent(authorities::add);
        setupClient.ifPresent(authorities::add);
        sa.setRights(authorities);

        var saJson = objectMapper.writeValueAsString(sa);

        mockMvc
            .perform(
                post(BASE_PATH)
                    .content(saJson)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user(new UserPrincipal(superAdmin)))
                    .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnErrorWhenDuplicatedDataOnCreation() throws Exception {
        // given
        var adminUser = getMockUser(MANAGE_USERS);
        var userDto = new UserDto();
        userDto.setEmail("test.email@sprest.de");
        userDto.setUserName("test_user");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setRights(Set.of());

        // when
        mockMvc.perform(
                post(BASE_PATH)
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user(new UserPrincipal(adminUser)))
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk());

        // expect
        mockMvc.perform(
                post(BASE_PATH)
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user(new UserPrincipal(adminUser)))
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(status().reason(is(String.format("Die Email %s wird bereits verwendet", userDto.getEmail()))));

        userDto.setEmail(userDto.getEmail().toUpperCase());
        mockMvc.perform(
                post(BASE_PATH)
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user(new UserPrincipal(adminUser)))
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(status().reason(is(String.format("Die Email %s wird bereits verwendet", userDto.getEmail()))));

        userDto.setEmail("different@email.com");
        mockMvc.perform(
                post(BASE_PATH)
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user(new UserPrincipal(adminUser)))
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(status().reason(is(String.format("Der Anmeldename %s wird bereits verwendet", userDto.getUserName()))));

        userDto.setUserName(userDto.getUserName().toUpperCase());
        mockMvc.perform(
                post(BASE_PATH)
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user(new UserPrincipal(adminUser)))
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(status().reason(is(String.format("Der Anmeldename %s wird bereits verwendet", userDto.getUserName()))));
    }

    @Test
    public void mustImpersonateUser() throws Exception {
        // given
        var adminUser = getMockUser(MANAGE_USERS);
        var user = new AppUser();
        user.setEmail("test.email@sprest.de");
        user.setUserName("test_user");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("pass");
        var right = new UserAuthority();
        right.setAuthority(MANAGE_ANNOUNCEMENTS);
        user.setRights(Set.of(userAuthorityRepository.save(right)));
        var userId = userRepository.save(user).getId();
        var adminId = adminUser.getId();
        var reason = "Very important reason";

        // when
        mockMvc.perform(
                post(BASE_PATH + "/impersonate/" + userId)
                    .with(user(new UserPrincipal(adminUser)))
                    .with(csrf())
                    .param("reason", reason))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(userId)))
            .andExpect(jsonPath("$.rights[0].authority", is(right.getAuthority())));

        // expect
        checkIfImpersonationSucceeded(adminId, userId, true);

        // when
        mockMvc.perform(
                post(BASE_PATH + "/stopImpersonation")
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(adminUser.getId())))
            .andExpect(jsonPath("$.rights[*].authority", containsInAnyOrder(MANAGE_ANNOUNCEMENTS, MANAGE_USERS)));

        // expect
        checkIfImpersonationSucceeded(adminId, userId, false);
    }

    private void checkIfImpersonationSucceeded(int adminId, int userId, boolean impersonationActive) throws Exception {
        mockMvc.perform(
                get("/users/current")
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(impersonationActive ? userId : adminId)));

        mockMvc.perform(
                get(BASE_PATH + "/" + userId) // requires MANAGE_USERS right
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().is(impersonationActive ? HttpStatus.FORBIDDEN.value() : HttpStatus.OK.value()));

        mockMvc.perform(
                get("/admin/employees")   // requires MANAGE_STAFF right
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().is(impersonationActive ? HttpStatus.OK.value() : HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void shouldReturnErrorWhenSuperAdminImpersonationAttempted() throws Exception {
        // given
        var adminUser = getMockUser(MANAGE_ANNOUNCEMENTS);
        var user = new AppUser();
        user.setEmail("test.email@sprest.de");
        user.setUserName("test_user");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("pass");
        var right = new UserAuthority();
        right.setAuthority(MANAGE_ANNOUNCEMENTS);
        user.setRights(Set.of(userAuthorityRepository.save(right)));
        var userId = userRepository.save(user).getId();

        // expect
        mockMvc.perform(
                post(BASE_PATH + "/impersonate/" + userId)
                    .with(user(new UserPrincipal(adminUser)))
                    .with(csrf())
                    .param("reason", "Very important reason"))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString(
                String.format("Sie dürfen nicht als Benutzer %s ausgeben, da dieser auch Superadmin ist.", user.getUserName()))));
    }
    @Test
    public void shouldReturnErrorWhenNoImpersonationReasonProvided() throws Exception {
        // given
        var adminUser = getMockUser(MANAGE_ANNOUNCEMENTS);
        var userId = 9;

        // expect
        mockMvc.perform(
                post(BASE_PATH + "/impersonate/" + userId)
                    .with(user(new UserPrincipal(adminUser)))
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(status().reason(containsString(
                "Required request parameter 'reason' for method parameter type String is not present")));
    }

    @Test
    public void shouldIgnoreCasingInAnAvailabilityCheck() throws Exception {
        // given
        var user = getMockUser(MANAGE_ANNOUNCEMENTS);
        user.setEmail("mail@mail.com");
        var savedUser = userRepository.save(user);

        // expect
        mockMvc.perform(
                        get(BASE_PATH + "/email-available/" + savedUser.getEmail().toUpperCase())
                                .with(user(new UserPrincipal(savedUser)))
                                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk()).andExpect(content().string("false"));
    }
}
