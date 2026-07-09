package ktb.week04.springboot.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CommentRequestDto {
    @NotBlank(message = "Text is required")
    private String commentText;
}
