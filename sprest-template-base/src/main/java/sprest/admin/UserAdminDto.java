package sprest.admin;

import sprest.user.dtos.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class UserAdminDto extends UserDto {

    @NotNull
    private int id;

}
