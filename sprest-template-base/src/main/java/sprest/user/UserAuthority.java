package sprest.user;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class UserAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_authority_seq")
    @GenericGenerator(name="user_authority_seq", strategy="increment")
    private Integer id;
    private String authority;
}
