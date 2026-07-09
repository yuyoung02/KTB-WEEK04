package ktb.week04.springboot.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostRequestDto {

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content text is required")
    private String text;

    private String image;

}
