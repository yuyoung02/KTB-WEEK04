package ktb.week04.springboot.integrationTest;

import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.repository.PostLikeRepository;
import ktb.week04.springboot.repository.PostRepository;
import ktb.week04.springboot.repository.UserRepository;
import ktb.week04.springboot.service.PostService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Testcontainers
class PostViewConcurrencyTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;


    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0");


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                mysql::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                mysql::getUsername
        );

        registry.add(
                "spring.datasource.password",
                mysql::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                () -> "com.mysql.cj.jdbc.Driver"
        );

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "create-drop"
        );
    }


    @BeforeEach
    void cleanUp() {

        // FK 때문에 자식 테이블부터 삭제
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("100명이 동시에 같은 게시글 조회 시 조회수 100 증가")
    void concurrentPostView()
            throws InterruptedException, ExecutionException {

        //given
        final int threadSize = 100;

        // 게시글 작성자 생성
        User user = new User(
                "test@test.com",
                "password",
                "testUser",
                null
        );

        User savedUser = userRepository.save(user);


        // 조회할 게시글 생성
        Post post = new Post(
                savedUser,
                StadiumCode.DAEJEON,
                "동시성 테스트 게시글",
                null,
                "동시성 테스트 내용"
        );

        Post savedPost = postRepository.save(post);

        Long postId = savedPost.getPostId();


        // 100개의 작업을 처리할 스레드 풀
        ExecutorService executorService =
                Executors.newFixedThreadPool(threadSize);


        // 모든 스레드가 준비됐는지 확인
        CountDownLatch readyLatch =
                new CountDownLatch(threadSize);


        // 동시에 출발시키기 위한 latch
        CountDownLatch startLatch =
                new CountDownLatch(1);


        // 모든 작업이 끝났는지 확인
        CountDownLatch doneLatch =
                new CountDownLatch(threadSize);


        // 각 작업의 성공/실패를 확인하기 위한 Future
        List<Future<?>> futures = new ArrayList<>();


        //when
        for (int i = 0; i < threadSize; i++) {

            Future<?> future = executorService.submit(() -> {

                try {

                    // 이 스레드는 준비 완료
                    readyLatch.countDown();

                    // 100개 스레드가 한꺼번에 출발할 때까지 대기
                    startLatch.await();


                    // 게시글 상세 조회
                    // 내부에서 post.increaseView() 실행
                    postService.getPost(postId);


                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);

                } finally {

                    // 작업 완료
                    doneLatch.countDown();
                }
            });

            futures.add(future);
        }


        // 100개 스레드가 준비될 때까지 기다림
        readyLatch.await();


        // 100개 스레드 동시 출발
        startLatch.countDown();


        // 100개 작업이 전부 끝날 때까지 기다림
        doneLatch.await();


        // 작업 중 예외가 있었는지 확인
        for (Future<?> future : futures) {
            future.get();
        }


        executorService.shutdown();


        //then
        // DB에서 다시 조회
        Post result = postRepository.findById(postId)
                .orElseThrow();


        // 100명이 조회했으므로 조회수도 100이어야 함
        assertThat(result.getViewNum())
                .isEqualTo((long) threadSize);
    }
}