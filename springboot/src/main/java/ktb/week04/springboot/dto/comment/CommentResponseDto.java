package ktb.week04.springboot.dto.comment;

import ktb.week04.springboot.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponseDto {

    private Long commentId;
    private String commentText;
    private String nickname;

    public CommentResponseDto(Comment comment){

        this.commentId = comment.getCommentId();
        this.commentText = comment.getCommentText();
        this.nickname = comment.getNickname();
    }
}
