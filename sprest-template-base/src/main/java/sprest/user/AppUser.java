package sprest.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import sprest.admin.UserAdminDto;
import sprest.user.dtos.UserDto;
import sprest.user.dtos.UserDtoWithId;
import sprest.user.dtos.UserSelfAdminDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
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
public class AppUser {

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

    @Column(length = 1000)
    private String firstName;

    @Column(length = 100)
    private String title;

    /**
     * comma separated list of access rights assigned to this app user
     * @see {@link AllAccessRights#getValues()} and enums ennotated with {@link sprest.api.AccessRightEnum}
     */
    @ManyToMany
    @Fetch(FetchMode.JOIN)
    private Set<AccessRight> accessRights;

    @JsonIgnore
    @Column(length = 64)
    private String passwordResetToken;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordResetTokenValidUntil;

    public AppUser(UserDto dto) {
        copyDto(dto);
    }

    public void copyDto(UserDto dto) {
        setActive(dto.isActive());
        setEmail(dto.getEmail() != null ? dto.getEmail().toLowerCase() : null);
        setFirstName(dto.getFirstName());
        setLastName(dto.getLastName());
        setAccessRights(dto.getAccessRights());
        setTitle(dto.getTitle());
        setUserName(dto.getUserName().toLowerCase());
    }

    public void copySelfAdminDto(UserSelfAdminDto dto) {
        setEmail(dto.getEmail());
        setFirstName(dto.getFirstName());
        setLastName(dto.getLastName());
        setTitle(dto.getTitle());
    }

    public UserDtoWithId toUserDtoWithId() {
        UserDtoWithId dto = new UserDtoWithId();
        dto.setId(getId());
        setCommonFields(dto);
        return dto;
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

        if (this.getAccessRights() != null) {
            userDto.setAccessRights(this.getAccessRights());
        }
    }

    public boolean hasRight(String right) {
        var rights = this.getAccessRights();
        for (var r : rights) {
            if (right.equals(r.getName())) {
                return true;
            }
        }
        return false;
    }

}
