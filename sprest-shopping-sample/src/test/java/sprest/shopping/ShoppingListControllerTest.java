package sprest.shopping;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import sprest.shopping.item.ItemDto;
import sprest.user.UserPrincipal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sprest.user.ShoppingRight.values.ACCESS_SHOPPING;

public class ShoppingListControllerTest extends ShoppingSampleTestBase {

    @Test
    public void testCreateItem() throws Exception {
        var user = getMockUser(ACCESS_SHOPPING);
        var dto = new ItemDto();
        dto.setName("Umbrella");
        dto.setStoreId(999);

        mockMvc
                .perform(post("/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .with(csrf())
                    .with(user(new UserPrincipal(user)))
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        dto.setStoreId(store.getId());

        mockMvc
                .perform(post("/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .with(csrf())
                    .with(user(new UserPrincipal(user)))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.creationDate", is(notNullValue())))
                .andExpect(jsonPath("$.store.id", is(store.getId())))
                .andExpect(jsonPath("$.store.name", is(store.getName())));
    }

    @Test
    public void testGetItemById() throws Exception {
        var user = getMockUser(ACCESS_SHOPPING);
        mockMvc
                .perform(get("/items/" + 999)
                    .with(user(new UserPrincipal(user))))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc
                .perform(get("/items/" + item.getId())
                    .with(user(new UserPrincipal(user))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId())))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.creationDate", is(notNullValue())))
                .andExpect(jsonPath("$.store.id", is(store.getId())))
                .andExpect(jsonPath("$.store.name", is(store.getName())));
    }

    @Test
    public void testGetItemsPage() throws Exception {
        var user = getMockUser(ACCESS_SHOPPING);
        mockMoreItems();

        mockMvc
                .perform(get("/items")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "name,asc")
                    .with(user(new UserPrincipal(user)))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.numberOfElements", is(5)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.content[*].name", contains("Boots", "Coat", "Gloves", "Hat", "Jeans")));

        mockMvc
                .perform(get("/items")
                    .param("page", "1")
                    .param("size", "3")
                    .param("sort", "creationDate,desc")
                    .with(user(new UserPrincipal(user)))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number", is(1)))
                .andExpect(jsonPath("$.size", is(3)))
                .andExpect(jsonPath("$.numberOfElements", is(2)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.content[1].name", is("Coat")));
    }

    @Test
    public void testGetItemsByStoreId() throws Exception {
        var user = getMockUser(ACCESS_SHOPPING);
        mockMoreItems();
        mockMoreStores();

        mockMvc
                .perform(get("/items/store/999")
                    .param("page", "0")
                    .param("size", "10")
                    .with(user(new UserPrincipal(user))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.numberOfElements", is(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)))
                .andExpect(jsonPath("$.content.length()", is(0)));

        mockMvc
                .perform(get("/items/store/" + store.getId())
                    .param("page", "0")
                    .param("size", "10")
                    .with(user(new UserPrincipal(user))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.numberOfElements", is(5)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.content.length()", is(5)))
                .andExpect(jsonPath("$.content[*].name", contains(
                        "Coat", "Boots", "Jeans", "Hat", "Gloves")));

        mockMvc
                .perform(get("/items/store/2")
                    .param("page", "0")
                    .param("size", "10")
                    .with(user(new UserPrincipal(user))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.numberOfElements", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].name", containsString("T-shirt")));
    }

}
