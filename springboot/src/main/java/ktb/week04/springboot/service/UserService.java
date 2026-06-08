package ktb.week04.springboot.service;

import jakarta.validation.Valid;
import ktb.week04.springboot.dto.user.*;
import ktb.week04.springboot.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    // user 데이터 담을 곳
    private final Map<Long, User> users = new HashMap<>();
    private Long userId = 1L;

    //로그인
    public LoginResponseDto login(@Valid @RequestBody LoginRequstDto loginRequest){

        User loginUser = null;

        // 이메일로 유저 찾기
        for (User user : users.values()) {
            if (user.getEmail().equals(loginRequest.getEmail())) {
                loginUser = user;
                break;
            }
        }

        // 이메일 없거나 비밀번호 틀림
        if (loginUser == null ||
                !loginUser.getPassword().equals(loginRequest.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "email or password invalid"
            );
        }

        //이메일, 비밀번호 빈칸시

        return new LoginResponseDto(loginUser);
    }

    //회원가입
    public UserResponseDto signup(@Valid @RequestBody SignupRequestDto userRequest){
        User user = new User(
                userId,
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getNickname(),
                userRequest.getImage()
        );

        // 이메일 중복 검사 로직 필요할 것 같음

        users.put(userId,user);
        userId ++;

        return new UserResponseDto(user);
    }

    //회원 정보 조회
    public UserResponseDto getUser(@PathVariable Long userId){
        if(!users.containsKey(userId)){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        User user = users.get(userId);


        if (user == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        return new UserResponseDto(user);
    }

    //회원 정보 수정
    public UserResponseDto patchUser(@PathVariable Long userId, @RequestBody UserPatchDto request){
        User user = users.get(userId);

        if ((!users.containsKey(userId)) || user == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        if (request.getNickname() != null) {
            user.changeNickname(request.getNickname());
        }

        if (request.getImage() != null) {
            user.changeImage(request.getImage());
        }

        if (request.getImage() == null && request.getNickname() ==null){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nothing changed"
            );
        }

        return new UserResponseDto(user);
    }


    //회원 비밀번호 수정 -> 비밀번호 재확인 로직
    public String changePassword(@PathVariable Long userId, @RequestBody PasswordRequestDto passwordRequest){

        if(users.get(userId)== null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        if (!users.get(userId).getPassword().equals(passwordRequest.getOriginalPwd())){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Password is wrong"
            );
        }

        if(!passwordRequest.getNewPwd().equals(passwordRequest.getOneMoreNewPwd())){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New Password and One more Password is different"
            );
        }

        users.get(userId).changePwd(passwordRequest.getNewPwd());

        return "Password Changed successfully";
    }

    //회원 삭제
    public String deleteUser(@PathVariable Long userId, @Valid @RequestBody UserDeleteRequstDto deleteRequst) {
        User user = users.get(userId);

        if (user == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        if(!user.getPassword().equals(deleteRequst.getPassword())){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Password is Wrong"
            );
        }

        users.remove(userId);

        return "Delete Success";
    }

}


