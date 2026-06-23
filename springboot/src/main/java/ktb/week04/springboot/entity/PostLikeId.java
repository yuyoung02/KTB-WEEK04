package ktb.week04.springboot.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class PostLikeId implements Serializable {
    private Long user;
    private Long post;
}