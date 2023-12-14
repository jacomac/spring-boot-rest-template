package sprest.announce;

import sprest.exception.NotFoundByUniqueKeyException;
import sprest.exception.UserInputValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    protected static Announcement announcement;

    private Announcement getAnnouncementInstance() {
        if (announcement == null) {
            var announcementList = announcementRepository.findAll();
            var a = announcementList.isEmpty() ? new Announcement() : announcementList.get(0);
            saveAnnouncement(a);
        }
        return announcement;
    }

    private void saveAnnouncement(Announcement a) {
        announcement = announcementRepository.save(a);
    }

    public Announcement getAnnouncement(boolean isForAdministration) {
        var a = getAnnouncementInstance();
        if (!isForAdministration)
            checkIfAnnouncementDateValid(a);

        return a;
    }

    private void checkIfAnnouncementDateValid(Announcement a) {
        var now = new Date();
        if (a.getEndDate().before(now) || a.getStartDate().after(now))
            throw new NotFoundByUniqueKeyException("Keine Ankündigung derzeit vorhanden");
    }

    public Announcement updateAnnouncement(Announcement dto) {
        var a = getAnnouncementInstance();
        if (dto.getEndDate().before(dto.getStartDate()))
            throw new UserInputValidationException("Zeitraumangabe falsch!");
        a.setHeading(dto.getHeading());
        a.setBodyHtml(dto.getBodyHtml());
        a.setStartDate(dto.getStartDate());
        a.setEndDate(dto.getEndDate());

        saveAnnouncement(a);
        return announcement;
    }
}
