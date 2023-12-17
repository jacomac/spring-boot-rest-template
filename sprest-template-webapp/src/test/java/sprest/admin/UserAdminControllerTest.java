package sprest.admin;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import sprest.ControllerTestBase;
import sprest.user.AppUser;
import sprest.user.AccessRight;
import sprest.user.UserPrincipal;
import sprest.user.BaseRight;
import sprest.user.dtos.UserDto;
import sprest.user.repositories.AccessRightRepository;
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
import static sprest.user.BaseRight.values.*;

/**
 * @author Konrad Wulf
 */
class UserAdminControllerTest extends ControllerTestBase {

    private final String BASE_PATH = "/admin/users";

    @Autowired
    AccessRightRepository accessRightRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    public void shouldGetUserById() throws Exception {
        var user = getMockUser(MANAGE_USERS);
        var rights = accessRightRepository.saveAll(user.getAccessRights());
        user.setPassword("password");
        Set<AccessRight> accessRightSet = new HashSet<>();
        rights.forEach(accessRightSet::add);
        user.setAccessRights(accessRightSet);
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
            .andExpect(jsonPath("$..*", Matchers.hasItem(BaseRight.MANAGE_USERS.toString())));
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
        var auAuthorities = new HashSet<AccessRight>();

        var manageUsers = accessRightRepository.findByName(MANAGE_USERS);
        manageUsers.ifPresent(auAuthorities::add);

        nu.setAccessRights(auAuthorities);

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

        var userAuthority = new AccessRight();
        userAuthority.setId(1);
        userAuthority.setName("MANAGE_ANNOUNCEMENTS");

        var userRights = user.getAccessRights();
        userRights.add(userAuthority);

        user.setAccessRights(userRights);

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
    public void shouldReturnErrorWhenDuplicatedDataOnCreation() throws Exception {
        // given
        var adminUser = getMockUser(MANAGE_USERS);
        var userDto = new UserDto();
        userDto.setEmail("test.email@sprest.de");
        userDto.setUserName("test_user");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setAccessRights(Set.of());

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
    public void shouldIgnoreCasingInAnAvailabilityCheck() throws Exception {
        // given
        var user = getMockUser(MANAGE_USERS);
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
