package ktb.week04.springboot.dto.post;

import ktb.week04.springboot.entity.Post;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PostListResponseDto {

    private Long userId;
    private Long postId;
    private String subject;
    private Long likeCount;
    private Long viewNum;
    private LocalDateTime date;

    public PostListResponseDto(Post post, Long likeCount){
        this.userId = post.getUser().getUserId();
        this.postId = post.getPostId();
        this.subject = post.getSubject();
        this.likeCount = likeCount;
        this.viewNum = post.getViewNum();
        this.date = post.getCreatedAt();
    }

}
