package sprest.announce;

import sprest.exception.NotFoundByUniqueKeyException;
import sprest.exception.UserInputValidationException;
import sprest.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AnnouncementServiceTest extends AnnouncementService {

    @Mock
    AnnouncementRepository announcementRepository;
    @InjectMocks
    AnnouncementService announcementService;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(announcementService, "announcement", null);
    }

    @Test
    public void mustGetAnnouncementNewlyCreated() {
        // given
        var announcement = getMockAnnouncement();

        // when
        Mockito.when(announcementRepository.findAll()).thenReturn(List.of());
        Mockito.when(announcementRepository.save(any(Announcement.class))).thenReturn(announcement);
        var foundAnnouncement = announcementService.getAnnouncement(false);

        // expect
        assertEquals(announcement, foundAnnouncement);
    }

    @Test
    public void mustGetAnnouncementFromDatabase() {
        // given
        var announcement = getMockAnnouncement();

        // when
        Mockito.when(announcementRepository.findAll()).thenReturn(List.of(announcement));
        Mockito.when(announcementRepository.save(any(Announcement.class))).thenReturn(announcement);
        var foundAnnouncement = announcementService.getAnnouncement(false);

        // expect
        assertEquals(announcement, foundAnnouncement);
    }

    @Test
    public void mustThrowExceptionWhenNoValidAnnouncementFound() {
        // given
        var announcement = getMockAnnouncement();
        announcement.setStartDate(DateUtils.addDays(new Date(), 2));

        // when
        Mockito.when(announcementRepository.findAll()).thenReturn(List.of());
        Mockito.when(announcementRepository.save(any(Announcement.class))).thenReturn(announcement);
        NotFoundByUniqueKeyException e = assertThrows(NotFoundByUniqueKeyException.class,
            () -> announcementService.getAnnouncement(false));

        // expect
        assertEquals("Keine Ankündigung derzeit vorhanden", e.getMessage());
    }

    @Test
    public void mustThrowExceptionWhenDtoDatesInvalid() {
        // given
        var announcement = getMockAnnouncement();
        var dto = new Announcement();
        dto.setHeading("New heading");
        dto.setBodyHtml("New content");
        dto.setStartDate(DateUtils.addDays(new Date(), 1));
        dto.setEndDate(DateUtils.addDays(new Date(), -1));

        // when
        Mockito.when(announcementRepository.findAll()).thenReturn(List.of());
        Mockito.when(announcementRepository.save(any(Announcement.class))).thenReturn(announcement);
        UserInputValidationException e = assertThrows(UserInputValidationException.class,
            () -> announcementService.updateAnnouncement(dto));

        // expect
        assertEquals("Zeitraumangabe falsch!", e.getMessage());
    }

    @Test
    public void mustUpdateAnnouncement() {
        // given
        var announcement = getMockAnnouncement();
        var dto = new Announcement();
        dto.setHeading("New heading");
        dto.setBodyHtml("New content");
        dto.setStartDate(DateUtils.addDays(new Date(), -1));
        dto.setEndDate(DateUtils.addDays(new Date(), 3));

        // when
        Mockito.when(announcementRepository.findAll()).thenReturn(List.of());
        Mockito.when(announcementRepository.save(any(Announcement.class))).thenReturn(announcement);
        var updatedAnnouncement = announcementService.updateAnnouncement(dto);

        // expect
        assertEquals(dto.getHeading(), updatedAnnouncement.getHeading());
        assertEquals(dto.getBodyHtml(), updatedAnnouncement.getBodyHtml());
        assertEquals(dto.getStartDate(), updatedAnnouncement.getStartDate());
        assertEquals(dto.getEndDate(), updatedAnnouncement.getEndDate());
    }

    private Announcement getMockAnnouncement() {
        var announcement = new Announcement();
        announcement.setId(1);
        announcement.setHeading("Heading");
        announcement.setBodyHtml("Content");
        announcement.setStartDate(DateUtils.addDays(new Date(), -1));
        announcement.setEndDate(DateUtils.addDays(new Date(), 1));

        return announcement;
    }
}
