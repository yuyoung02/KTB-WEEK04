package ktb.week04.springboot.service;

import jakarta.validation.Valid;
import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.repository.CommentRepository;
import ktb.week04.springboot.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;
import ktb.week04.springboot.dto.user.*;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserService {


    private final UserRepository userRepository;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public UserService(UserRepository userRepository, PostRepository postRepository,
                       CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    //로그인
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequstDto loginRequest){

        User loginUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "email or password invalid"
                ));

        // 비밀번호 틀림
        if (!loginUser.getPassword().equals(loginRequest.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "email or password invalid"
            );
        }

        return new LoginResponseDto(loginUser);
    }

    //회원가입
    public UserResponseDto signup(SignupRequestDto userRequest){

        // 이메일 중복 검사
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        if (userRepository.existsByNickname(userRequest.getNickname())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nickname already exists"
            );
        }

        User user = new User(
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getNickname(),
                userRequest.getImage()
        );

        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser);
    }

    //회원 정보 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new UserResponseDto(user);
    }

    //회원 정보 수정
    public UserResponseDto patchUser(Long userId, UserPatchDto request){
        if (!userId.equals(request.getRequestUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only owner can modify user"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        if (request.getNickname() != null) {
            // 기존 닉네임과 같으면 중복 검사 제외
            String newNickname = request.getNickname();
            if(!user.getNickname().equals(newNickname)) {
                if (userRepository.existsByNickname(request.getNickname())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Nickname already exists");
                }
            }
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
    public String changePassword(Long userId,PasswordRequestDto passwordRequest){

        if (!userId.equals(passwordRequest.getRequestUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only owner can change password"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getPassword().equals(passwordRequest.getOriginalPwd())){
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

        user.changePwd(passwordRequest.getNewPwd());

        return "Password Changed successfully";
    }

    //회원 삭제
    public String deleteUser(Long userId, UserDeleteRequstDto deleteRequest) {

        if (!userId.equals(deleteRequest.getRequestUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only owner can delete user"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        if(!user.getPassword().equals(deleteRequest.getPassword())){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Password is Wrong"
            );
        }

        List<Post> posts = postRepository.findByUserAndDeletedAtIsNull(user);
        for (Post post : posts) {
            post.delete();
        }

        List<Comment> comments = commentRepository.findByUserAndDeletedAtIsNull(user);
        for (Comment comment : comments) {
            comment.delete();
        }

        user.delete();

        return "Delete Success";
    }

}


