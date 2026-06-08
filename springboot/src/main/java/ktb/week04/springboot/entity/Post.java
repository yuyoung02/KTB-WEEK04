package ktb.week04.springboot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.PrimitiveIterator;

@Getter
@NoArgsConstructor
public class Post {

    private Long userId;
    private Long postId;
    private String subject;
    private String image;
    private String text;
    private Long likeNum;
    private Long viewNum;
    private String date;

    public Post(Long userId, Long postId, String subject, String image, String text, Long likeNum,Long viewNum, String date) {
        this.userId = userId;
        this.postId = postId;
        this.subject = subject;
        this.image = image;
        this.text = text;
        this.likeNum = likeNum;
        this.viewNum = viewNum;
        this.date = date;

    }

    public void changeText(String changedText){
        this.text = changedText;
    }

    public void changeImage(String changedImage){
        this.image = changedImage;
    }

    public Long increaseLike() {
        this.likeNum++;
        return likeNum;
    }

    // 디비 구성후 내가 누른 좋아요만 취소되게
    public Long decreaseLike() {
        if (this.likeNum > 0) {
            this.likeNum--;
        }

        return likeNum;
    }

    public void increaseView() {
        this.viewNum++;
    }
}
