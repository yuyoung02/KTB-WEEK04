package ktb.week04.springboot.service;

import ktb.week04.springboot.dto.vote.StadiumVoteRankingDto;
import ktb.week04.springboot.dto.vote.StadiumVoteRequestDto;
import ktb.week04.springboot.dto.vote.StadiumVoteResponseDto;
import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.StadiumVote;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.repository.StadiumVoteRepository;
import ktb.week04.springboot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("구장 투표 서비스 테스트")
class StadiumVoteServiceTest {

    @InjectMocks
    private StadiumVoteService stadiumVoteService;

    @Mock
    private StadiumVoteRepository stadiumVoteRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "vote@test.com",
                "encodedPassword",
                "투표유저",
                null
        );
    }

    @Test
    @DisplayName("첫 투표를 하면 이번 달 투표가 저장된다.")
    // 첫 투표 생성과 저장 결과 확인
    void vote_success_newVote() {
        StadiumVoteRequestDto request = requestOf(StadiumCode.JAMSIL);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stadiumVoteRepository.findByUserAndVoteMonth(eq(testUser), anyString()))
                .thenReturn(Optional.empty());
        when(stadiumVoteRepository.save(any(StadiumVote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StadiumVoteResponseDto result = stadiumVoteService.vote(1L, request);

        ArgumentCaptor<StadiumVote> captor = ArgumentCaptor.forClass(StadiumVote.class);
        verify(stadiumVoteRepository).save(captor.capture());
        assertEquals(StadiumCode.JAMSIL, captor.getValue().getStadiumCode());
        assertEquals(currentVoteMonth(), captor.getValue().getVoteMonth());
        assertEquals(StadiumCode.JAMSIL, result.getStadiumId());
    }

    @Test
    @DisplayName("기존 투표가 있으면 선택한 구장으로 변경된다.")
    // 기존 투표의 구장 변경 확인
    void vote_success_changeVote() {
        StadiumVote existingVote =
                new StadiumVote(testUser, currentVoteMonth(), StadiumCode.JAMSIL);
        StadiumVoteRequestDto request = requestOf(StadiumCode.GOCHEOK);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stadiumVoteRepository.findByUserAndVoteMonth(eq(testUser), anyString()))
                .thenReturn(Optional.of(existingVote));
        when(stadiumVoteRepository.save(existingVote)).thenReturn(existingVote);

        StadiumVoteResponseDto result = stadiumVoteService.vote(1L, request);

        assertEquals(StadiumCode.GOCHEOK, existingVote.getStadiumCode());
        assertEquals(StadiumCode.GOCHEOK, result.getStadiumId());
        verify(stadiumVoteRepository).save(existingVote);
    }

    @Test
    @DisplayName("전체 구장에는 투표할 수 없다.")
    // ALL 구장 투표 요청의 400 응답 확인
    void vote_fail_allStadium() {
        StadiumVoteRequestDto request = requestOf(StadiumCode.ALL);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> stadiumVoteService.vote(1L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(userRepository, stadiumVoteRepository);
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 투표하면 404가 발생한다.")
    // 존재하지 않는 사용자의 404 응답 확인
    void vote_fail_userNotFound() {
        StadiumVoteRequestDto request = requestOf(StadiumCode.JAMSIL);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> stadiumVoteService.vote(1L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(stadiumVoteRepository);
    }

    @Test
    @DisplayName("이번 달 내 투표가 있으면 투표 정보가 반환된다.")
    // 이번 달 기존 투표 조회 결과 확인
    void getMyVote_success() {
        StadiumVote vote =
                new StadiumVote(testUser, currentVoteMonth(), StadiumCode.DAEJEON);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stadiumVoteRepository.findByUserAndVoteMonth(eq(testUser), anyString()))
                .thenReturn(Optional.of(vote));

        Optional<StadiumVoteResponseDto> result = stadiumVoteService.getMyVote(1L);

        assertTrue(result.isPresent());
        assertEquals(StadiumCode.DAEJEON, result.get().getStadiumId());
    }

    @Test
    @DisplayName("이번 달 투표가 없으면 빈 결과가 반환된다.")
    // 첫 투표 사용자의 빈 조회 결과 확인
    void getMyVote_empty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stadiumVoteRepository.findByUserAndVoteMonth(eq(testUser), anyString()))
                .thenReturn(Optional.empty());

        Optional<StadiumVoteResponseDto> result = stadiumVoteService.getMyVote(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("기존 투표를 취소하면 투표가 삭제된다.")
    // 기존 투표 취소 시 삭제 호출 확인
    void cancelMyVote_success() {
        StadiumVote vote =
                new StadiumVote(testUser, currentVoteMonth(), StadiumCode.SUWON);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stadiumVoteRepository.findByUserAndVoteMonth(eq(testUser), anyString()))
                .thenReturn(Optional.of(vote));

        stadiumVoteService.cancelMyVote(1L);

        verify(stadiumVoteRepository).delete(vote);
    }

    @Test
    @DisplayName("투표가 없는 상태에서 취소해도 오류가 발생하지 않는다.")
    // 미투표 상태의 취소 요청이 안전한지 확인
    void cancelMyVote_withoutVote() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stadiumVoteRepository.findByUserAndVoteMonth(eq(testUser), anyString()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> stadiumVoteService.cancelMyVote(1L));

        verify(stadiumVoteRepository, never()).delete(any(StadiumVote.class));
    }

    @Test
    @DisplayName("랭킹은 득표수와 비율을 계산해 상위 3개 구장을 반환한다.")
    // 득표수, 백분율 계산과 TOP3 정렬 확인
    void getRankings_success() {
        List<StadiumVote> votes = List.of(
                voteOf(StadiumCode.JAMSIL),
                voteOf(StadiumCode.JAMSIL),
                voteOf(StadiumCode.GOCHEOK),
                voteOf(StadiumCode.DAEJEON)
        );
        when(stadiumVoteRepository.findByVoteMonth(anyString())).thenReturn(votes);

        List<StadiumVoteRankingDto> result = stadiumVoteService.getRankings();

        assertEquals(3, result.size());
        assertEquals(StadiumCode.JAMSIL, result.get(0).getStadiumId());
        assertEquals(2L, result.get(0).getVoteCount());
        assertEquals(50.0, result.get(0).getPercentage());
        assertEquals(1L, result.get(1).getVoteCount());
        assertEquals(25.0, result.get(1).getPercentage());
        assertEquals(1L, result.get(2).getVoteCount());
        assertEquals(25.0, result.get(2).getPercentage());
    }

    private StadiumVoteRequestDto requestOf(StadiumCode stadiumCode) {
        StadiumVoteRequestDto request = mock(StadiumVoteRequestDto.class);
        when(request.getStadiumId()).thenReturn(stadiumCode);
        return request;
    }

    private StadiumVote voteOf(StadiumCode stadiumCode) {
        return new StadiumVote(testUser, currentVoteMonth(), stadiumCode);
    }

    private String currentVoteMonth() {
        return YearMonth.now(ZoneId.of("Asia/Seoul")).toString();
    }
}
