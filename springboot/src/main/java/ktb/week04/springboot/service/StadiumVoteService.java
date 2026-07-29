package ktb.week04.springboot.service;

import ktb.week04.springboot.dto.vote.StadiumVoteRankingDto;
import ktb.week04.springboot.dto.vote.StadiumVoteRequestDto;
import ktb.week04.springboot.dto.vote.StadiumVoteResponseDto;
import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.StadiumVote;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.repository.StadiumVoteRepository;
import ktb.week04.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StadiumVoteService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final StadiumVoteRepository stadiumVoteRepository;
    private final UserRepository userRepository;

    public StadiumVoteResponseDto vote(
            Long currentUserId,
            StadiumVoteRequestDto request
    ) {
        if (request.getStadiumId() == StadiumCode.ALL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ALL cannot be voted"
            );
        }

        User user = findUserById(currentUserId);
        String voteMonth = getCurrentVoteMonth();

        StadiumVote vote = stadiumVoteRepository
                .findByUserAndVoteMonth(user, voteMonth)
                .orElseGet(() -> new StadiumVote(
                        user,
                        voteMonth,
                        request.getStadiumId()
                ));

        vote.changeStadiumCode(request.getStadiumId());

        return new StadiumVoteResponseDto(
                stadiumVoteRepository.save(vote)
        );
    }

    @Transactional(readOnly = true)
    public StadiumVoteResponseDto getMyVote(Long currentUserId) {
        User user = findUserById(currentUserId);

        StadiumVote vote = stadiumVoteRepository
                .findByUserAndVoteMonth(user, getCurrentVoteMonth())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vote not found"
                ));

        return new StadiumVoteResponseDto(vote);
    }

    @Transactional(readOnly = true)
    public List<StadiumVoteRankingDto> getRankings() {
        List<StadiumVote> votes = stadiumVoteRepository
                .findByVoteMonth(getCurrentVoteMonth());

        Map<StadiumCode, Long> voteCounts = votes.stream()
                .collect(Collectors.groupingBy(
                        StadiumVote::getStadiumCode,
                        Collectors.counting()
                ));

        long totalVoteCount = votes.size();

        return Arrays.stream(StadiumCode.values())
                .filter(stadiumCode -> stadiumCode != StadiumCode.ALL)
                .map(stadiumCode -> {
                    long voteCount =
                            voteCounts.getOrDefault(stadiumCode, 0L);
                    double percentage = totalVoteCount == 0
                            ? 0.0
                            : Math.round(
                                    voteCount * 1000.0 / totalVoteCount
                            ) / 10.0;

                    return new StadiumVoteRankingDto(
                            stadiumCode,
                            voteCount,
                            percentage
                    );
                })
                .sorted(
                        Comparator.comparingLong(
                                StadiumVoteRankingDto::getVoteCount
                        ).reversed()
                )
                .limit(3)
                .toList();
    }

    private String getCurrentVoteMonth() {
        return YearMonth.now(KOREA_ZONE).toString();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }
}
