package ktb.week04.springboot.dto.post;

import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.Post;
import lombok.Getter;

@Getter
public class PostCreateResponseDto {

    private Long userId;
    private StadiumCode stadiumId;
    private Long postId;

    public PostCreateResponseDto(Post post){
        this.userId = post.getUser().getUserId();
        this.stadiumId = post.getStadiumCode();
        this.postId = post.getPostId();

    }

}