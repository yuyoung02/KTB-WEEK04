package ktb.week04.springboot.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class PasswordRequestDto {
    @NotBlank(message = "Original Password is required")
    private String originalPwd;

    @NotBlank(message = "New Password is required")
    private String newPwd;

    @NotBlank(message = "New Password is required One more time")
    private String oneMoreNewPwd;
}
