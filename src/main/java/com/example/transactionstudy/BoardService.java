package com.example.transactionstudy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public Board get(int id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시물 없음"));
    }

    @Transactional
    public Board like(int id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시물 없음"));
        board.like();
        return board;
    }
}
