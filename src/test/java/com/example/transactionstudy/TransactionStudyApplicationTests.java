package com.example.transactionstudy;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TransactionStudyApplicationTests {
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        resetLikes();
    }

    @AfterEach
    void tearDown() {
        resetLikes();
    }

    @Order(1)
    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> applicationContext.getBean(BoardController.class));
        assertDoesNotThrow(() -> applicationContext.getBean(BoardService.class));
        assertDoesNotThrow(() -> applicationContext.getBean(BoardRepository.class));
    }

    @Order(2)
    @Test
    @DisplayName("1번 board 좋아요 수 초기화 확인")
    void verifyResetLikes() throws Exception {
        mockMvc.perform(get("/boards/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.likes").value(0));
    }

    @Order(3)
    @Test
    @DisplayName("좋아요")
    void like() throws Exception {
        mockMvc.perform(put("/boards/1/like"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.likes").value(1));
    }

    @Order(4)
    @Test
    @DisplayName("좋아요 10회 순차 요청")
    void sequence_like_10_times() throws Exception {
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(put("/boards/1/like"))
                    .andExpect(jsonPath("$.likes").value(i));
        }
    }

    /**
     * ObjectOptimisticLockingFailureException(낙관락 예외) 발생시 재귀 호출
     * 재시도를 일정 횟수 이상으로 요청할 경우 예외를 발생시킨다.
     * 따라서, 일부 사용자의 요청이 완료되지 않는다. - 탈락
     */
    @Order(5)
    @Test
    @DisplayName("좋아요 동시 요청 - 타임아웃")
    void concurrency_like_with_timeout() throws Exception {
        int numberOfThreads = 400;
        ExecutorService service = Executors.newFixedThreadPool(200); // 스프링 스레드풀 기본 값인 200으로 설정
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    mockMvc.perform(put("/boards/1/like/optimistic-lock-with-timeout"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        mockMvc.perform(get("/boards/1"))
                .andExpect(jsonPath("$.likes").value(numberOfThreads));
    }

    /**
     * ObjectOptimisticLockingFailureException(낙관락 예외) 발생시 재귀 호출
     * 예외가 발생하지 않을 때까지 무한히 재시도.
     * 사용자의 요청을 높은 확률로 전부 처리할 수 있을 것으로 보이나, 처리 시간이 오래 걸림.
     * 스프링 스레드풀은 기본적으로 타임아웃이 1분으로 설정되어있어 1분 동안 요청을 처리하지 못 할 경우 타임아웃 처리가 될 것으로 추측된다.
     */
    @Order(6)
    @Test
    @DisplayName("좋아요 동시 요청 - 완료될때까지 재시도")
    void concurrency_like_util_complete() throws Exception {
        int numberOfThreads = 400;
        ExecutorService service = Executors.newFixedThreadPool(200); // 스프링 스레드풀 기본 값인 200으로 설정
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    mockMvc.perform(put("/boards/1/like/optimistic-lock"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        mockMvc.perform(get("/boards/1"))
                .andExpect(jsonPath("$.likes").value(numberOfThreads));
    }

    void resetLikes() {
        boardRepository.findAll().forEach(board -> {
            board.resetLikes();
            boardRepository.saveAndFlush(board);
        });
    }
}
