package ktb.week04.springboot.dto.comment;

import ktb.week04.springboot.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponseDto {

    private Long commentId;
    private String commentText;
    private Long userId;
    private String nickname;
    private String image;

    public CommentResponseDto(Comment comment, String imageUrl){

        this.commentId = comment.getCommentId();
        this.commentText = comment.getCommentText();
        this.userId = comment.getUser().getUserId();
        this.nickname = comment.getUser().getNickname();
        this.image = imageUrl;
    }
}
