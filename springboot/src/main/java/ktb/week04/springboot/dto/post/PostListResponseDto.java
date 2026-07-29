package ktb.week04.springboot.dto.post;

import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.Post;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PostListResponseDto {

    private Long userId;
    private String userNickname;
    private String image;
    private Long postId;
    private StadiumCode stadiumId;
    private String subject;
    private Long likeCount;
    private Long commentCount;
    private Long viewNum;
    private LocalDateTime date;

    public PostListResponseDto(Post post, Long likeCount, Long commentCount){
        this.userId = post.getUser().getUserId();
        this.userNickname = post.getUser().getNickname();
        this.image = post.getUser().getImage();
        this.postId = post.getPostId();
        this.stadiumId = post.getStadiumCode();
        this.subject = post.getSubject();
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewNum = post.getViewNum();
        this.date = post.getCreatedAt();
    }

}
