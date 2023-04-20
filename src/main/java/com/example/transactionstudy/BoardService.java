package com.example.transactionstudy;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public Board get(int id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시물 없음"));
    }

    public Board likeOptimistic(int id) {
        int numberOfTries = 10;
        while (numberOfTries-- > 0) {
            try {
                Board board = boardRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("게시물 없음"));
                board.like();
                boardRepository.saveAndFlush(board);
                return board;
            } catch (ObjectOptimisticLockingFailureException e) {
                System.out.printf("재시도 %d\n", numberOfTries);
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(100, 200));
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        throw new RuntimeException("좋아요 처리 실패");
    }
}
