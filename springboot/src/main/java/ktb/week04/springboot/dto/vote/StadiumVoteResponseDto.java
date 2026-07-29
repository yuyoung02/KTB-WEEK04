package ktb.week04.springboot.dto.vote;

import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.StadiumVote;
import lombok.Getter;

@Getter
public class StadiumVoteResponseDto {

    private Long voteId;
    private Long userId;
    private String voteMonth;
    private StadiumCode stadiumId;

    public StadiumVoteResponseDto(StadiumVote vote) {
        this.voteId = vote.getVoteId();
        this.userId = vote.getUser().getUserId();
        this.voteMonth = vote.getVoteMonth();
        this.stadiumId = vote.getStadiumCode();
    }
}
