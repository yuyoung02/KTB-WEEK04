package ktb.week04.springboot.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserDeleteRequstDto {
    private Long requestUserId;

    @NotBlank(message = "Password is required")
    private String password;
}
