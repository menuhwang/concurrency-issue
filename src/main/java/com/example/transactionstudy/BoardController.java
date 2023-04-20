package com.example.transactionstudy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/{id}")
    public Board get(@PathVariable int id) {
        return boardService.get(id);
    }

    @PutMapping("/{id}/like")
    public Board like(@PathVariable int id) {
        return boardService.likeOptimistic(id);
    }
}
