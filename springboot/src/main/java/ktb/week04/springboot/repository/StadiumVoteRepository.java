package ktb.week04.springboot.repository;

import ktb.week04.springboot.entity.StadiumVote;
import ktb.week04.springboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StadiumVoteRepository
        extends JpaRepository<StadiumVote, Long> {

    Optional<StadiumVote> findByUserAndVoteMonth(
            User user,
            String voteMonth
    );

    List<StadiumVote> findByVoteMonth(String voteMonth);
}
