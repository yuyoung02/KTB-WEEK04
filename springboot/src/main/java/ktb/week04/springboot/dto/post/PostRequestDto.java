package ktb.week04.springboot.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ktb.week04.springboot.entity.Enum.StadiumCode;
import lombok.Getter;

@Getter
public class PostRequestDto {

    @NotNull(message = "Stadium is required")
    private StadiumCode stadiumId;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content text is required")
    private String text;

    private String image;

}
