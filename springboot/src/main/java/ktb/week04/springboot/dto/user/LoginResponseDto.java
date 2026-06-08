package ktb.week04.springboot.dto.user;

import ktb.week04.springboot.entity.User;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private Long id;

    public LoginResponseDto(User user){
        this.id = user.getId();
        //토큰
    }
}
