package sprest.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import sprest.admin.UserAdminDto;
import sprest.user.dtos.UserDto;
import sprest.user.dtos.UserSelfAdminDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userName"}, name = "UC_user_name"),
    @UniqueConstraint(columnNames = {"email"}, name = "UC_user_email")
}, indexes = {
    @Index(columnList = "userName", name = "IDX_user_name"),
    @Index(columnList = "email", name = "IDX_user_email"),
    @Index(name = "IDX_lastName", columnList = "lastName")
})
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @GenericGenerator(name="user_seq", strategy="increment")
    private Integer id;

    @NotNull
    private boolean active = true;

    @NotNull
    @Column(length = 50)
    private String userName;

    @JsonIgnore
    @NotNull
    @Column(length = 100)
    private String password;

    @Column(length = 1000)
    private String email;

    @Column(length = 1000)
    private String lastName;

    /**
     * Namenszusatz
     */
    @Column(length = 200)
    private String nameAffix;

    /**
     * Namensvorsatz
     */
    @Column(length = 200)
    private String namePrefix;

    @Column(length = 1000)
    private String firstName;

    @Column(length = 100)
    private String title;

    /**
     * Rechte (kommasepariert)
     *
     * @see UserRight
     */
    @ManyToMany
    @Fetch(FetchMode.JOIN)
    private Set<UserAuthority> rights;

    @JsonIgnore
    @Column(length = 64)
    private String passwordResetToken;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordResetTokenValidUntil;

    public User(UserDto dto) {
        copyDto(dto);
    }

    public void copyDto(UserDto dto) {
        setActive(dto.isActive());
        setEmail(dto.getEmail() != null ? dto.getEmail().toLowerCase() : null);
        setFirstName(dto.getFirstName());
        setLastName(dto.getLastName());
        setRights(dto.getRights());
        setTitle(dto.getTitle());
        setUserName(dto.getUserName().toLowerCase());
    }

    public void copySelfAdminDto(UserSelfAdminDto dto) {
        setEmail(dto.getEmail());
        setFirstName(dto.getFirstName());
        setLastName(dto.getLastName());
        setTitle(dto.getTitle());
    }

    public UserDao toDao() {
        UserDao dao = new UserDao();
        dao.setId(getId());
        setCommonFields(dao);
        return dao;
    }

    public UserAdminDto toAdminDto() {
        UserAdminDto dto = new UserAdminDto();
        dto.setId(getId());
        setCommonFields(dto);
        return dto;
    }

    private void setCommonFields(UserDto userDto) {
        userDto.setActive(isActive());
        userDto.setEmail(getEmail());
        userDto.setFirstName(getFirstName());
        userDto.setLastName(getLastName());
        userDto.setTitle(getTitle());
        userDto.setUserName(getUserName());

        if (getRights() != null) {
            userDto.setRights(getRights());
        }
    }

    public boolean hasRight(String right) {
        var rights = getRights();
        for (var r : rights) {
            if (right.equals(r.getAuthority())) {
                return true;
            }
        }
        return false;
    }

}
