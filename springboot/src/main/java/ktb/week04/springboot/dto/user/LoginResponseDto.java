package ktb.week04.springboot.dto.user;

import ktb.week04.springboot.entity.User;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private Long userId;

    private String accessToken;
    public LoginResponseDto(User user, String accessToken){
        this.userId = user.getUserId();
        //토큰
        this.accessToken = accessToken;
    }
}
