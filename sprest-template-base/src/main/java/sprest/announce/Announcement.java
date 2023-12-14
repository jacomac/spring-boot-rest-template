package sprest.announce;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "announcement_seq")
    @GenericGenerator(name="announcement_seq", strategy="increment")
    private Integer id;

    @NotNull
    @Value("${sprest.announce.heading}")
    @Column(length=200)
    private String heading = "Ankündigung von Wartungsarbeiten";

    @NotNull
    @Value("${sprest.announce.body}")
    @Column(length=1000)
    private String bodyHtml = "Ab diesen <b>Donnerstag 18 Uhr</b> steht dieses System hier wegen <i>Wartungsarbeiten</i> vorraussichtl. für 2 Stunden nicht zur Verfügung";

    @NotNull
    @Value("${sprest.announce.start}")
    private Date startDate = new Date();

    @NotNull
    @Value("${sprest.announce.end}")
    private Date endDate = new Date();
}


