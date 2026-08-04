package ktb.week04.springboot.controller;

import jakarta.validation.Valid;
import ktb.week04.springboot.dto.vote.StadiumVoteRankingDto;
import ktb.week04.springboot.dto.vote.StadiumVoteRequestDto;
import ktb.week04.springboot.dto.vote.StadiumVoteResponseDto;
import ktb.week04.springboot.security.CustomUserDetails;
import ktb.week04.springboot.service.StadiumVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stadiumVotes")
@RequiredArgsConstructor
public class StadiumVoteController {

    private final StadiumVoteService stadiumVoteService;

    @PutMapping
    public StadiumVoteResponseDto vote(
            @Valid @RequestBody StadiumVoteRequestDto request,
            Authentication authentication
    ) {
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        return stadiumVoteService.vote(
                userDetails.getUserId(),
                request
        );
    }

    @GetMapping("/me")
    public ResponseEntity<StadiumVoteResponseDto> getMyVote(
            Authentication authentication
    ) {
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        return stadiumVoteService.getMyVote(userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/rankings")
    public List<StadiumVoteRankingDto> getRankings() {
        return stadiumVoteService.getRankings();
    }
}
