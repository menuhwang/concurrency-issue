package com.example.transactionstudy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;

    private static final int TIMEOUT = 10;

    @GetMapping("/{id}")
    public Board get(@PathVariable int id) {
        return boardService.get(id);
    }

    @PutMapping("/{id}/like")
    public Board like(@PathVariable int id) {
        return boardService.like(id);
    }

    @PutMapping("/{id}/like/optimistic-lock-with-timeout")
    public Board likeWithTimeout(@PathVariable int id, @Nullable Integer timeout) {
        timeout = timeout == null ? 0 : timeout;
        if (timeout >= TIMEOUT) throw new RuntimeException("재시도 횟수 초과");
        try {
            return boardService.like(id);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.debug("낙관락 예외 발생 재시도 : {} of {}", timeout, TIMEOUT);
            return this.likeWithTimeout(id, timeout + 1);
        }
    }

    @PutMapping("/{id}/like/optimistic-lock")
    public Board likeUtilComplete(@PathVariable int id) {
        try {
            return boardService.like(id);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.debug("낙관락 예외 발생 재시도");
            return this.likeUtilComplete(id);
        }
    }
}
