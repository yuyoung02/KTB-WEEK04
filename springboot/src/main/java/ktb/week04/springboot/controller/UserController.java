package ktb.week04.springboot.controller;

import jakarta.validation.Valid;
import ktb.week04.springboot.dto.user.*;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.security.CustomUserDetails;
import ktb.week04.springboot.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


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
    @GetMapping("/me")
    public UserResponseDto getUser(Authentication authentication){

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Long currentUserId = userDetails.getUserId();

        return userService.getUser(currentUserId);
    }

    //회원 정보 수정 -> 내정보만 수정가능하도록
    @PatchMapping("/me")
    public UserResponseDto patchUser(@Valid @RequestBody UserPatchDto request, Authentication authentication){

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userService.patchUser(request, userDetails.getUserId());
    }

    // 회원 비밀번호 수정
    @PatchMapping("/me/password")
    public String changePassword(@Valid @RequestBody PasswordRequestDto passwordRequest, Authentication authentication){

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userService.changePassword(passwordRequest, userDetails.getUserId());
    }

    //회원 삭제
    @DeleteMapping("/me")
    public String deleteUser(@Valid @RequestBody UserDeleteRequstDto deleteRequst, Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userService.deleteUser(deleteRequst, userDetails.getUserId());
    }

}
