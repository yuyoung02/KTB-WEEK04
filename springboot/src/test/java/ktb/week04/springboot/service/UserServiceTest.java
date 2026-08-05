package ktb.week04.springboot.service;

import ktb.week04.springboot.dto.user.*;
import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.repository.CommentRepository;
import ktb.week04.springboot.repository.PostRepository;
import ktb.week04.springboot.repository.UserRepository;
import ktb.week04.springboot.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User 도메인 테스트")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private S3Service s3Service;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "test@test.com",
                "testEncodedPassword",
                "테스트닉네임",
                null
        );
    }

    @Test
    @DisplayName("로그인 성공 시 accessToken이 반환되어야 한다.")
    void login_success() {
        // given
        LoginRequstDto request =
                new LoginRequstDto("test@test.com", "test!1234");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("test!1234", "testEncodedPassword")).thenReturn(true);

        when(jwtTokenProvider.createToken(
                testUser.getUserId(),
                testUser.getEmail(),
                testUser.getRole()
        )).thenReturn("testToken");

        // when
        LoginResponseDto result = userService.login(request);

        // then
        assertEquals("testToken", result.getAccessToken());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일이 존재하지 않으면 401 예외가 발생한다.")
    void login_fail_emailNotFound() {
        // given
        LoginRequstDto request = new LoginRequstDto("wrong@test.com", "test!1234");

        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows( ResponseStatusException.class, () -> userService.login(request));

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호가 틀리면 401 예외가 발생한다.")
    void login_fail_passwordMismatch() {
        // given
        LoginRequstDto request = new LoginRequstDto("test@test.com", "wrongPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("wrongPassword", "testEncodedPassword")).thenReturn(false);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.login(request));

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    @DisplayName("회원가입 성공 시 비밀번호가 인코딩되어 저장된다.")
    void signup_success() {
        // given
        SignupRequestDto request = new SignupRequestDto(
                "new@test.com",
                "test!1234",
                "새닉네임"
        );

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        when(userRepository.existsByNickname("새닉네임")).thenReturn(false);

        when(passwordEncoder.encode("test!1234")).thenReturn("testEncodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserResponseDto result = userService.signup(request, null);

        // then
        assertEquals("new@test.com", result.getEmail());
        assertEquals("새닉네임", result.getNickname());

        verify(passwordEncoder).encode("test!1234");

        verify(userRepository).save(argThat(user -> user.getPassword().equals("testEncodedPassword")));}

    @Test
    @DisplayName("회원가입 실패 - 이메일이 중복되면 409 예외가 발생한다.")
    void signup_fail_duplicateEmail() {
        // given
        SignupRequestDto request = new SignupRequestDto(
                "test@test.com",
                "test!1234",
                "닉네임"
        );

        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.signup(request, null)
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임이 중복되면 409 예외가 발생한다.")
    void signup_fail_duplicateNickname() {
        // given
        SignupRequestDto request = new SignupRequestDto(
                "new@test.com",
                "test!1234",
                "중복닉네임"
        );

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        when(userRepository.existsByNickname("중복닉네임")).thenReturn(true);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.signup(request, null)
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("회원 조회 성공 시 회원 정보가 반환된다.")
    void getUser_success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // when
        UserResponseDto result = userService.getUser(1L);

        // then
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getNickname(), result.getNickname());
    }

    @Test
    @DisplayName("회원 조회 실패 - 회원이 없으면 404 예외가 발생한다.")
    void getUser_fail_userNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getUser(1L));

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("회원 정보 수정 성공 시 닉네임이 변경된다.")
    void patchUser_success_nickname() {
        // given
        UserPatchDto request = new UserPatchDto("변경닉네임", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        when(userRepository.existsByNickname("변경닉네임")).thenReturn(false);

        // when
        UserResponseDto result = userService.patchUser(request, null, 1L);

        // then
        assertEquals("변경닉네임", result.getNickname());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 변경할 값이 없으면 400 예외가 발생한다.")
    void patchUser_fail_nothingChanged() {
        // given
        UserPatchDto request = new UserPatchDto(null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.patchUser(request, null, 1L)
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 닉네임이 중복되면 409 예외가 발생한다.")
    void patchUser_fail_duplicateNickname() {
        // given
        UserPatchDto request = new UserPatchDto("중복닉네임", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        when(userRepository.existsByNickname("중복닉네임")).thenReturn(true);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.patchUser(request, null, 1L));

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("비밀번호 변경 성공 시 새 비밀번호가 인코딩되어 변경된다.")
    void changePassword_success() {
        // given
        PasswordRequestDto request = new PasswordRequestDto(
                "old!1234",
                "new!1234",
                "new!1234"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("old!1234", "testEncodedPassword")).thenReturn(true);

        when(passwordEncoder.encode("new!1234")).thenReturn("newEncodedPassword");

        // when
        String result = userService.changePassword(request, 1L);

        // then
        assertEquals("Password Changed successfully", result);
        assertEquals("newEncodedPassword", testUser.getPassword());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호가 틀리면 401 예외가 발생한다.")
    void changePassword_fail_wrongOriginalPassword() {
        // given
        PasswordRequestDto request = new PasswordRequestDto(
                "wrongPassword",
                "new!1234",
                "new!1234"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("wrongPassword", "testEncodedPassword")).thenReturn(false);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.changePassword(request, 1L));

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 확인이 다르면 400 예외가 발생한다.")
    void changePassword_fail_newPasswordMismatch() {
        // given
        PasswordRequestDto request = new PasswordRequestDto(
                "old!1234",
                "new!1234",
                "different!1234"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("old!1234", "testEncodedPassword")).thenReturn(true);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.changePassword(request, 1L)
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @DisplayName("회원 삭제 성공 시 회원, 게시글, 댓글이 삭제 처리된다.")
    void deleteUser_success() {
        // given
        UserDeleteRequstDto request = new UserDeleteRequstDto("test!1234");

        Post post = mock(Post.class);
        Comment comment = mock(Comment.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("test!1234", "testEncodedPassword")).thenReturn(true);

        when(postRepository.findByUserAndDeletedAtIsNull(testUser)).thenReturn(List.of(post));

        when(commentRepository.findByUserAndDeletedAtIsNull(testUser)).thenReturn(List.of(comment));

        // when
        String result = userService.deleteUser(request, 1L);

        // then
        assertEquals("Delete Success", result);
        //회원 삭제 시, 게시글과 댓글까지 잘 삭제되는지
        verify(post).delete();
        verify(comment).delete();
        //soft delete 처리 잘 되는지
        assertNotNull(testUser.getDeletedAt());
    }

    @Test
    @DisplayName("회원 삭제 실패 - 비밀번호가 틀리면 401 예외가 발생한다.")
    void deleteUser_fail_wrongPassword() {
        // given
        UserDeleteRequstDto request = new UserDeleteRequstDto("wrongPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("wrongPassword", "testEncodedPassword")).thenReturn(false);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.deleteUser(request, 1L)
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
