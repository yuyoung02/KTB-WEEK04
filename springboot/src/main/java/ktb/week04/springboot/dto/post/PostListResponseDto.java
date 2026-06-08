package ktb.week04.springboot.dto.post;

import ktb.week04.springboot.entity.Post;
import lombok.Getter;

@Getter
public class PostListResponseDto {

    private Long userId;
    private Long postId;
    private String subject;
    private Long likeNum;
    private Long viewNum;
    private String date;

    public PostListResponseDto(Post post){
        this.userId = post.getUserId();
        this.postId = post.getPostId();
        this.subject = post.getSubject();
        this.likeNum = post.getLikeNum();
        this.viewNum = post.getViewNum();
        this.date = post.getDate();
    }

}
