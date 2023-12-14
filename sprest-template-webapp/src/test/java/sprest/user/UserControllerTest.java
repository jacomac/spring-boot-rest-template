package sprest.user;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sprest.ControllerTestBase;
import sprest.user.repositories.UserAuthorityRepository;
import sprest.user.repositories.UserRepository;

import java.util.HashSet;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sprest.user.UserRight.values.MANAGE_ALL;
import static sprest.user.UserRight.values.MANAGE_ANNOUNCEMENTS;

class UserControllerTest extends ControllerTestBase {

    private static final String BASE_PATH = "/users";

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserAuthorityRepository userAuthorityRepository;

    @Test
    public void mustReturnUserPrincipalWithActiveRights() throws Exception {
        // given
        var u = getTestUser(List.of(MANAGE_ANNOUNCEMENTS));

        // expect
        mockMvc.perform(
            get(BASE_PATH + "/current")
                .with(user(new UserPrincipal(u))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rights[0].authority", Matchers.is(MANAGE_ANNOUNCEMENTS)));
    }

    @Test
    public void mustGetAvailableUserRights() throws Exception {
        var u = getMockUser();

        // expect
        mockMvc.perform(
                get(BASE_PATH + "/rights")
                    .with(user(new UserPrincipal(u))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThan(0))));
    }

    @Test
    public void mustReturnUserWithSetupClientsRightWithoutModuleSubscription() throws Exception {
        // given
        var u = getTestUser(List.of(MANAGE_ANNOUNCEMENTS));

        // expect
        mockMvc.perform(
                get(BASE_PATH + "/current")
                    .with(user(new UserPrincipal(u))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rights[0].authority", Matchers.is(MANAGE_ANNOUNCEMENTS)));
    }

    private User getTestUser(List<String> authorityNames) {
        var user = new User();
        user.setUserName("sirius-black");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("p@assw0rd");
        var authorities = new HashSet<UserAuthority>();
        authorityNames.forEach(authority -> authorities.add(
            userAuthorityRepository.findByAuthority(authority).orElseThrow()
        ));
        user.setRights(authorities);

        return userRepository.save(user);
    }
}