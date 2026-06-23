package ktb.week04.springboot.dto.user;

import ktb.week04.springboot.entity.User;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private String email;
    private Long userId;
    private String nickname;
    private String image;

    public UserResponseDto(User user){
        this.email = user.getEmail();
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.image = user.getImage();
    }


}
