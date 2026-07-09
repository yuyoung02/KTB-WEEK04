package ktb.week04.springboot.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDeleteRequstDto {
    @NotBlank(message = "Password is required")
    private String password;
}
