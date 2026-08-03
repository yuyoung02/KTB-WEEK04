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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    // 이미지처리
    private final S3Service s3Service;

    public UserService(UserRepository userRepository, PostRepository postRepository,
                       CommentRepository commentRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                       S3Service s3Service) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.s3Service = s3Service;
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
    public UserResponseDto signup(SignupRequestDto userRequest,
                                  MultipartFile image){

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

        // 프로필 이미지가 있으면 profiles 경로에 업로드한다.
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.upload(image, "profiles");
        }

        User user = new User(
                userRequest.getEmail(),
                encodedPwd,
                userRequest.getNickname(),
                imageUrl
        );

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
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
        return toUserResponse(user);
    }

    //회원 정보 수정
    public UserResponseDto patchUser(UserPatchDto request,
                                     MultipartFile image, Long currentUserId){

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        boolean hasImage = image != null && !image.isEmpty();

        // 닉네임도 없고 새 이미지도 없으면 변경할 내용이 없다.
        if (request.getNickname() == null && !hasImage) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nothing changed"
            );
        }

        if (request.getNickname() != null) {
            String newNickname = request.getNickname();

            // 기존 닉네임과 다를 때만 중복 검사
            if (!user.getNickname().equals(newNickname)
                    && userRepository.existsByNickname(newNickname)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Nickname already exists"
                );
            }

            user.changeNickname(newNickname);
        }

        if (hasImage) {
            String previousImageUrl = user.getImage();

            // 새로운 이미지를 먼저 업로드한다.
            String newImageUrl =
                    s3Service.upload(image, "profiles");

            // DB에는 새 이미지 URL을 저장한다.
            user.changeImage(newImageUrl);

            // 트랜잭션이 성공한 뒤 기존 이미지를 삭제한다.
            deleteImageAfterCommit(previousImageUrl);
        }

        return toUserResponse(user);
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

        String profileImageUrl = user.getImage();

        user.delete();

        deleteImageAfterCommit(profileImageUrl);

        return "Delete Success";
    }

    // DB 변경이 정상적으로 커밋된 뒤 기존 S3 이미지를 삭제
    private void deleteImageAfterCommit(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        s3Service.delete(imageUrl);
                    }
                }
        );
    }

    private UserResponseDto toUserResponse(User user) {
        return new UserResponseDto(
                user,
                s3Service.createPresignedGetUrl(user.getImage())
        );
    }

}

