package ktb.week04.springboot.dto.post;


import ktb.week04.springboot.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class PostResponseDto {

    private Long userId;
    private String nickname;
    private Long postId;
    private String subject;
    private String image;
    private String text;
    private Long likeCount;
    private Long viewNum;
    private LocalDateTime date;

    public PostResponseDto(Post post, Long likeCount){
        this.userId = post.getUser().getUserId();
        this.nickname = post.getUser().getNickname();
        this.postId = post.getPostId();
        this.subject = post.getSubject();
        this.image = post.getImage();
        this.text = post.getText();
        this.likeCount = likeCount;
        this.viewNum = post.getViewNum();
        this.date = post.getCreatedAt();
    }



}
