package ktb.week04.springboot.entity;

import jakarta.persistence.*;
import ktb.week04.springboot.entity.Enum.StadiumCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "StadiumVotes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_stadium_vote_user_month",
                columnNames = {"user_id", "vote_month"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StadiumVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(name = "voteMonth", nullable = false, length = 7)
    private String voteMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "stadiumCode", nullable = false, length = 30)
    private StadiumCode stadiumCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public StadiumVote(User user, String voteMonth, StadiumCode stadiumCode) {
        this.user = user;
        this.voteMonth = voteMonth;
        this.stadiumCode = stadiumCode;
    }

    public void changeStadiumCode(StadiumCode stadiumCode) {
        this.stadiumCode = stadiumCode;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
