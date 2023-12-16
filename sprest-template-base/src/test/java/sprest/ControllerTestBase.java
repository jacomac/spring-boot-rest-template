package sprest;

import com.fasterxml.jackson.databind.ObjectMapper;
import sprest.user.AppUser;
import sprest.user.AccessRight;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@SpringBootTest
@Transactional
public class ControllerTestBase {

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(sharedHttpSession())
            .apply(springSecurity())
            .build();
    }

    @SafeVarargs
    protected final <T> AppUser getMockUser(T... rights) {
        AtomicInteger index = new AtomicInteger(1);
        Set<AccessRight> authorities = Arrays.stream(rights).map(x -> {
            var authority = new AccessRight();
            authority.setId(index.getAndIncrement());
            authority.setName(x.toString());
            return authority;
        }).collect(Collectors.toSet());

        var user = new AppUser();
        user.setId(9999);
        user.setUserName("sirius-black");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("pass");
        user.setAccessRights(authorities);

        return user;
    }
}
