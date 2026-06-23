package ktb.week04.springboot.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class CommentId implements Serializable {
    private Long post;
    private Long commentId;
}