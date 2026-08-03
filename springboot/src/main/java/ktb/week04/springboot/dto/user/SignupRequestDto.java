package ktb.week04.springboot.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import jakarta.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Nickname is required")
    private String nickname;

}
