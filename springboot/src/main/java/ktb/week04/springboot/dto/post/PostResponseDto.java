package ktb.week04.springboot.dto.post;


import ktb.week04.springboot.entity.Post;
import lombok.Getter;


@Getter
public class PostResponseDto {

    private Long userId;
    private Long postId;
    private String subject;
    private String image;
    private String text;
    private Long likeNum;
    private Long viewNum;
    private String date;

    public PostResponseDto(Post post){
        this.userId = post.getUserId();
        this.postId = post.getPostId();
        this.subject = post.getSubject();
        this.image = post.getImage();
        this.text = post.getText();
        this.likeNum = post.getLikeNum();
        this.viewNum = post.getViewNum();
        this.date = post.getDate();
    }



}
