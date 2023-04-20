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

    @Order(5)
    @Test
    @DisplayName("좋아요 동시 요청")
    void concurrency_like_with_timeout() throws Exception {
        int numberOfThreads = 200;
        ExecutorService service = Executors.newFixedThreadPool(200); // 스프링 스레드풀 기본 값인 200으로 설정
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    mockMvc.perform(put("/boards/1/like"));
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
