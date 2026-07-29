package ktb.week04.springboot.dto.vote;

import ktb.week04.springboot.entity.Enum.StadiumCode;
import lombok.Getter;

@Getter
public class StadiumVoteRankingDto {

    private StadiumCode stadiumId;
    private Long voteCount;
    private double percentage;

    public StadiumVoteRankingDto(
            StadiumCode stadiumId,
            Long voteCount,
            double percentage
    ) {
        this.stadiumId = stadiumId;
        this.voteCount = voteCount;
        this.percentage = percentage;
    }
}
