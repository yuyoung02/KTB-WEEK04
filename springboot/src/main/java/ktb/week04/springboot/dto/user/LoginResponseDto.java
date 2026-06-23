package ktb.week04.springboot.dto.user;

import ktb.week04.springboot.entity.User;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private Long userId;

    public LoginResponseDto(User user){
        this.userId = user.getUserId();
        //토큰
    }
}
