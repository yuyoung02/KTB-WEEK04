package ktb.week04.springboot.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class PasswordRequestDto {

    private Long requestUserId;

    @NotBlank(message = "Original Password is required")
    private String originalPwd;

    @NotBlank(message = "New Password is required")
    private String newPwd;

    @NotBlank(message = "New Password is required One more time")
    private String oneMoreNewPwd;
}
