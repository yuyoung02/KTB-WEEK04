package ktb.week04.springboot.dto.user;

import lombok.Getter;
@Getter
public class UserPatchDto {
    private Long requestUserId;
    private String nickname;
    private String image;
}
