package ktb.week04.springboot.entity;

import ktb.week04.springboot.entity.Enum.StadiumCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "stadium_code", nullable = false, length = 30)
    private StadiumCode stadiumCode;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(length = 255)
    private String image;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private Long viewNum = 0L;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;


    public Post(User user, StadiumCode stadiumCode, String subject, String image, String text) {
        this.user = user;
        this.stadiumCode = stadiumCode;
        this.subject = subject;
        this.image = image;
        this.text = text;
        this.viewNum = 0L;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void delete(){
        this.deletedAt = LocalDateTime.now();
    }

    public void changeStadiumCode(StadiumCode stadiumCode) {
        this.stadiumCode = stadiumCode;
    }

    public void changeText(String changedText){
        this.text = changedText;
    }

    public void changeImage(String changedImage){
        this.image = changedImage;
    }

    public void changeSubject(String changedSubject){
        this.subject = changedSubject;
    }

    // public void increaseView() {
//        this.viewNum++;
//    }


}
