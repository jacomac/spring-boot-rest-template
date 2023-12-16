package sprest.user;

import jakarta.validation.constraints.NotNull;

import sprest.user.dtos.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDtoWithId extends UserDto {

    @NotNull
    private int id;

}
