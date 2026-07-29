package ktb.week04.springboot.dto.vote;

import jakarta.validation.constraints.NotNull;
import ktb.week04.springboot.entity.Enum.StadiumCode;
import lombok.Getter;

@Getter
public class StadiumVoteRequestDto {

    @NotNull(message = "Stadium is required")
    private StadiumCode stadiumId;
}
