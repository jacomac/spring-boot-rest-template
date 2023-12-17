package sprest.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Data
@Entity
@NoArgsConstructor
public class AccessRight {

    public AccessRight(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "right_seq")
    @GenericGenerator(name="right_seq", strategy="increment")
    private Integer id;

    /**
     * one of the rights in {@link AllAccessRights#getValues()}
     */
    private String name;
}
