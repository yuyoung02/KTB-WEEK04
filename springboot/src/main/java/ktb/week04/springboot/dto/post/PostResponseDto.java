package ktb.week04.springboot.dto.post;


import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class PostResponseDto {

    private Long userId;
    private String nickname;
    private Long postId;
    private StadiumCode stadiumId;
    private String subject;
    private String image;
    private String authorImage;
    private String text;
    private Long likeCount;
    private Long viewNum;
    private LocalDateTime date;

    public PostResponseDto(Post post, Long likeCount, String imageUrl, String authorImageUrl){
        this.userId = post.getUser().getUserId();
        this.nickname = post.getUser().getNickname();
        this.postId = post.getPostId();
        this.stadiumId = post.getStadiumCode();
        this.subject = post.getSubject();
        this.image = imageUrl;
        this.authorImage = authorImageUrl;
        this.text = post.getText();
        this.likeCount = likeCount;
        this.viewNum = post.getViewNum();
        this.date = post.getCreatedAt();
    }



}
