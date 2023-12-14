package sprest.user;

import javax.validation.constraints.NotNull;

import sprest.user.dtos.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDao extends UserDto {
	
    @NotNull
    private int id;

}
