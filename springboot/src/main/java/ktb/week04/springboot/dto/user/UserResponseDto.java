package ktb.week04.springboot.dto.user;

import ktb.week04.springboot.entity.User;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private String email;
    private Long id;
    private String nickname;
    private String image;

    public UserResponseDto(User user){
        this.email = user.getEmail();
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.image = user.getImage();
    }


}
