package sprest.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponse {
    private String password;
}
