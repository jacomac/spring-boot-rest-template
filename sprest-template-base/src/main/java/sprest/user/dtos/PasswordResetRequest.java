package sprest.user.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 */
@Getter
@Setter
public class PasswordResetRequest {
    @NotNull
    private String email;
}
