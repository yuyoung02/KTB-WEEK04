package ktb.week04.springboot.service;

import jakarta.validation.Valid;
import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.Enum.Role;
import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.repository.CommentRepository;
import ktb.week04.springboot.repository.PostRepository;
import ktb.week04.springboot.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
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


    // 레포
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // 비밀번호 해시 처리
    private final PasswordEncoder passwordEncoder;

    //jwt
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PostRepository postRepository,
                       CommentRepository commentRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //로그인
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequstDto loginRequest){

        User loginUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "email or password invalid"
                ));

        // 비밀번호 틀림 (비밀번호 해시 추가)
        if (!passwordEncoder.matches(loginRequest.getPassword(), loginUser.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "email or password invalid"
            );
        }

        //jwt
        String accessToken = jwtTokenProvider.createToken(
                loginUser.getUserId(),
                loginUser.getEmail(),
                loginUser.getRole()
        );

        return new LoginResponseDto(loginUser, accessToken);
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

        // 비밀번호 해시 추가
        String encodedPwd = passwordEncoder.encode(userRequest.getPassword());

        User user = new User(
                userRequest.getEmail(),
                encodedPwd,
                userRequest.getNickname(),
                userRequest.getImage()
        );

        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser);
    }

    //회원 정보 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long currentUserId){

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
        return new UserResponseDto(user);
    }

    //회원 정보 수정
    public UserResponseDto patchUser(UserPatchDto request, Long currentUserId){

        User user = userRepository.findById(currentUserId)
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
    public String changePassword(PasswordRequestDto passwordRequest, Long currentUserId){

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(passwordRequest.getOriginalPwd(), user.getPassword())){
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

        String encodedNewPwd = passwordEncoder.encode(passwordRequest.getNewPwd());
        user.changePwd(encodedNewPwd);

        return "Password Changed successfully";
    }

    //회원 삭제
    public String deleteUser(UserDeleteRequstDto deleteRequest, Long currentUserId) {

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        if(!passwordEncoder.matches(deleteRequest.getPassword(), user.getPassword())){
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


