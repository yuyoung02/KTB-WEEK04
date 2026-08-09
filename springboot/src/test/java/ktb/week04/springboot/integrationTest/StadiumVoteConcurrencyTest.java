package ktb.week04.springboot.integrationTest;

import ktb.week04.springboot.dto.vote.StadiumVoteRequestDto;
import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.StadiumVote;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.repository.StadiumVoteRepository;
import ktb.week04.springboot.repository.UserRepository;
import ktb.week04.springboot.service.StadiumVoteService;
import ktb.week04.springboot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class StadiumVoteConcurrencyTest {

    @Autowired
    private StadiumVoteService stadiumVoteService;

    @Autowired
    private StadiumVoteRepository stadiumVoteRepository;

    @Autowired
    private UserRepository userRepository;

    // 테스트용 mysql 컨테이너
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", mysql::getJdbcUrl);

        registry.add("spring.datasource.username", mysql::getUsername);

        registry.add("spring.datasource.password", mysql::getPassword);

    }

//    @Test
//    @DisplayName("Testcontainers MySQL 연결 확인")
//    void mysqlConnectionTest() {
//        System.out.println(mysql.getJdbcUrl());
//    }

    @Test
    @DisplayName("100명의 사용자가 동시에 투표시 100건 저장")
    void multipleUsersVoteConcurrency() throws InterruptedException, ExecutionException {

        //given
        final int threadSize = 100;
        // 100개의 작업을 동시에 처리할 수 있는 고정 크기 스레드 풀
        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
        //100개의 작업이 끝날때까지 기다리는 카운터 (100개의 작업이 모두 끝나는 시점을 확인
        CountDownLatch countDownLatch = new CountDownLatch(threadSize);

        //100명의 테스트유저 만들기
        List<User> testUsers = new ArrayList<>();

        for(int i = 0 ; i < threadSize ; i ++){

            User user = new User(
                    "test"+ i + "@test.com",
                    "password",
                    "testUser"+ i,
                    null
            );

            User saveUser = userRepository.save(user);
            testUsers.add(saveUser);
        }

        //when
        List<Future<?>> futures = new ArrayList<>();

        for(int j = 0 ; j < threadSize ; j ++){
            final int idx = j;
            Future<?> future = executorService.submit(() -> {
                try {
                    User user = testUsers.get(idx);

                    StadiumVoteRequestDto request = new StadiumVoteRequestDto(StadiumCode.DAEJEON);

                    stadiumVoteService.vote(user.getUserId(), request);

                } finally {
                    countDownLatch.countDown();
                }
            });

            futures.add(future);

        }

        countDownLatch.await();

        for (Future<?> ft : futures) {
            ft.get();
        }

        executorService.shutdown();

        //then
        String voteMonth = YearMonth.now(ZoneId.of("Asia/Seoul")).toString();

        List<StadiumVote> votes = stadiumVoteRepository.findByVoteMonth(voteMonth);

        assertThat(votes).hasSize(threadSize);

    }

    @BeforeEach
    void cleanUp() {
        stadiumVoteRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("같은 사용자가 동시에 10번 투표해도 1건만 저장")
    void sameUserVoteConcurrency()
            throws InterruptedException {

        // given
        final int threadSize = 10;

        ExecutorService executorService =
                Executors.newFixedThreadPool(threadSize);

        // 10개 작업이 모두 준비됐는지 확인
        CountDownLatch readyLatch =
                new CountDownLatch(threadSize);

        // 10개 작업을 동시에 출발시키기 위한 latch
        CountDownLatch startLatch =
                new CountDownLatch(1);

        // 10개 작업이 모두 끝났는지 확인
        CountDownLatch doneLatch =
                new CountDownLatch(threadSize);

        User user = new User(
                "same@test.com",
                "password",
                "sameUser",
                null
        );

        User savedUser = userRepository.save(user);

        StadiumVoteRequestDto request =
                new StadiumVoteRequestDto(StadiumCode.DAEJEON);

        List<Future<?>> futures = new ArrayList<>();


        // when
        for (int i = 0; i < threadSize; i++) {

            Future<?> future = executorService.submit(() -> {
                try {

                    // "나는 실행 준비됐어"
                    readyLatch.countDown();

                    // startLatch가 열릴 때까지 여기서 기다림
                    startLatch.await();

                    stadiumVoteService.vote(
                            savedUser.getUserId(),
                            request
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);

                } finally {

                    doneLatch.countDown();
                }
            });

            futures.add(future);
        }

        // 10개 스레드가 전부 준비될 때까지 기다림
        readyLatch.await();

        // 카운트 1 -> 0
        // 기다리고 있던 10개 스레드를 한꺼번에 출발시킴
        startLatch.countDown();

        // 10개 작업이 모두 끝날 때까지 기다림
        doneLatch.await();


        // 작업 중 발생한 예외 확인
        int exceptionCount = 0;

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                exceptionCount++;
            }
        }

        executorService.shutdown();

        System.out.println("exceptionCount = " + exceptionCount);


        // then
        String voteMonth =
                YearMonth.now(ZoneId.of("Asia/Seoul")).toString();

        long voteCount =
                stadiumVoteRepository
                        .countByUserAndVoteMonth(savedUser, voteMonth);

        assertThat(voteCount).isEqualTo(1L);

        System.out.println("발생한 작업 예외 수 = " + exceptionCount);
    }

}