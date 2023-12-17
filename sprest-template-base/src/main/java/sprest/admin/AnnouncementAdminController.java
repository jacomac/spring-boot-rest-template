package sprest.admin;

import sprest.announce.Announcement;
import sprest.announce.AnnouncementService;
import sprest.api.RequiredAccessRight;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import static sprest.user.BaseRight.values.MANAGE_ANNOUNCEMENTS;


@Tag(name = "Announcement Admin Controller", description = "API to configure an announcement for the end users within a given time frame, requires the MANAGE_ANNOUNCEMENTS privilege")
@RestController
@RequestMapping("/admin/announcement")
@RequiredAccessRight(MANAGE_ANNOUNCEMENTS)
public class AnnouncementAdminController {

    private final AnnouncementService announcementService;

    public AnnouncementAdminController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public Announcement getAnnouncement() {
        return announcementService.getAnnouncement(true);
    }

    @PutMapping
    public Announcement updateAnnouncement (
        @Valid @RequestBody Announcement announcement
    ) {
        return announcementService.updateAnnouncement(announcement);
    }
}