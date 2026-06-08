package ktb.week04.springboot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class User {

    private String email;
    private String password;
    private Long id;
    private String nickname;
    private String image;

    public User(Long id, String email, String password, String nickname, String image) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.image = image;
    }

    public void changeNickname(String nickname){
        this.nickname = nickname;
    }

    public void changeImage(String image){
        this.image = image;
    }

    public void changePwd(String password){
        this.password = password;
    }
}
