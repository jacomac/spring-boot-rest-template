package sprest.user.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UserSelfAdminDto {

    @Size(max = 1000)
    private String email;

    @NotNull
    @Size(max = 1000)
    private String lastName;

    @NotNull
    @Size(max = 1000)
    private String firstName;

    @Size(max = 100)
    private String title;

}
