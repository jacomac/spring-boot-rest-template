package sprest.user.dtos;

import sprest.user.AccessRight;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UserDto extends UserSelfAdminDto {

    /**
     * Aktiv-Flag
     */
    @NotNull
    private boolean active = true;

    @NotNull
    @Size(max = 50)
    private String userName;

    /**
     * Rechte (kommasepariert)
     */
    private Set<AccessRight> accessRights;

}
