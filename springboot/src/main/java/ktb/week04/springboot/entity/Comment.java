package ktb.week04.springboot.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Comment {

    private Long commentId;
    private String commentText;
    private String nickname;

    public Comment(Long commentId, String commentText, String nickname){
        this.commentId = commentId;
        this.commentText =commentText;
        this.nickname = nickname;
    }

    public void updateCommentText(String commentText) {
        this.commentText = commentText;
    }

}
