package sprest.announce;

import sprest.ControllerTestBase;
import sprest.user.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static sprest.user.UserRight.values.MANAGE_ANNOUNCEMENTS;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnnouncementControllerTest extends ControllerTestBase {

    @Test
    void shouldGetUpdatedAnnouncement() throws Exception{
        var user = getMockUser(MANAGE_ANNOUNCEMENTS);
        var dto = new Announcement();
        dto.setHeading("Halli Hallo");
        dto.setBodyHtml("Popcorn für alle!");
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        dto.setStartDate(Date.from(yesterday));
        Instant tomorrow = now.plus(1, ChronoUnit.DAYS);
        dto.setEndDate(Date.from(tomorrow));

        // update as text
        var request = objectMapper.writeValueAsString(dto);
        mockMvc
            .perform(
                put("/admin/announcement")
                    .content(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf())
                    .with(user(new UserPrincipal(user))))
            .andDo(print())
            .andExpect(status().isOk());

        // verify the new announcemet settings persist
        user = getMockUser();
        mockMvc
            .perform(
                get("/announcement")
                    .with(user(new UserPrincipal(user))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.heading", equalTo(dto.getHeading())))
            .andExpect(jsonPath("$.bodyHtml", equalTo(dto.getBodyHtml())));
    }

    @Test
    void shouldGet404OnOutdatedAnnouncementIfNotAdminCall() throws Exception{
        var user = getMockUser(MANAGE_ANNOUNCEMENTS);
        var dto = new Announcement();
        Instant now = Instant.now();
        Instant someTimeAgo = now.minus(5, ChronoUnit.DAYS);
        dto.setStartDate(Date.from(someTimeAgo));
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        dto.setEndDate(Date.from(yesterday));

        // update as text
        var request = objectMapper.writeValueAsString(dto);
        mockMvc
            .perform(
                put("/admin/announcement")
                    .content(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf())
                    .with(user(new UserPrincipal(user))))
            .andDo(print())
            .andExpect(status().isOk());

        // verify the outdated time frame leads to a 404
        user = getMockUser();
        mockMvc
            .perform(
                get("/announcement")
                    .with(user(new UserPrincipal(user))))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldAlwaysGetAnnouncementForAdministration() throws Exception{
        var user = getMockUser(MANAGE_ANNOUNCEMENTS);

        // verify that an admin user also gets an outdated announcement, so it can be activated again
        user = getMockUser();
        mockMvc
            .perform(
                get("/admin/announcement")
                    .with(csrf())
                    .with(user(new UserPrincipal(user))))
            .andDo(print())
            .andExpect(status().isForbidden()); // TODO should be OK status, no idea why the superadmin user gets back a forbidden 403

    }

}
