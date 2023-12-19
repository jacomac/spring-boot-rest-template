package sprest.shopping;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import sprest.shopping.store.StoreDto;
import sprest.user.UserPrincipal;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sprest.user.ShoppingRight.values.ACCESS_SHOPPING;

public class StoreControllerTest extends ShoppingSampleTestBase {

    @Test
    public void testCreateStore() throws Exception {
        var user = getMockUser(ACCESS_SHOPPING);
        var dto = new StoreDto();
        dto.setName("Farmer's Market");

        mockMvc
                .perform(post("/stores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .with(csrf())
                    .with(user(new UserPrincipal(user)))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(dto.getName())));
    }

    @Test
    public void testGetStoreById() throws Exception {
        var user = getMockUser(ACCESS_SHOPPING);
        mockMvc
                .perform(get("/stores/" + 9999999)
                    .with(user(new UserPrincipal(user))))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc
                .perform(get("/stores/" + store.getId())
                    .with(user(new UserPrincipal(user))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(store.getId())))
                .andExpect(jsonPath("$.name", is(store.getName())));
    }

    @Test
    public void testGetStoresInPages() throws Exception {
        var user = getMockUser(ACCESS_SHOPPING);
        mockMoreStores();

        mockMvc
                .perform(get("/stores")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "name,asc")
                    .with(user(new UserPrincipal(user)))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.numberOfElements", is(3)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }
}
