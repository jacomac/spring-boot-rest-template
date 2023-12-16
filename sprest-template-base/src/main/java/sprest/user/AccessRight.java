package sprest.user;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Data
@Entity
public class AccessRight {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "right_seq")
    @GenericGenerator(name="right_seq", strategy="increment")
    private Integer id;
    private String name;
}
