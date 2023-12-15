package sprest.user;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Data
@Entity
public class UserAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_authority_seq")
    @GenericGenerator(name="user_authority_seq", strategy="increment")
    private Integer id;
    private String authority;
}
