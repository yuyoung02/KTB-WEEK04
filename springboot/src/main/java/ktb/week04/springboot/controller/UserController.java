package ktb.week04.springboot.controller;

import jakarta.validation.Valid;
import ktb.week04.springboot.dto.user.*;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

   private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //로그인
    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequstDto loginRequest){
        return userService.login(loginRequest);
    }

    //회원가입
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto signup(@Valid @RequestBody SignupRequestDto userRequest){
        return userService.signup(userRequest);
    }

    //회원 정보 조회 -> 나만의 것 조회 가능하도
    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable Long userId){
        return userService.getUser(userId);
    }

    //회원 정보 수정 -> 내정보만 수정가능하도록
    @PatchMapping("/{userId}")
    public UserResponseDto patchUser(@PathVariable Long userId, @Valid @RequestBody UserPatchDto request){
        return userService.patchUser(userId,request);
    }

    // 회원 비밀번호 수정
    @PatchMapping("/{userId}/password")
    public String changePassword(@PathVariable Long userId, @Valid @RequestBody PasswordRequestDto passwordRequest){
        return userService.changePassword(userId, passwordRequest);
    }

    //회원 삭제
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Long userId, @Valid @RequestBody UserDeleteRequstDto deleteRequst) {
        return userService.deleteUser(userId,deleteRequst);
    }

}
