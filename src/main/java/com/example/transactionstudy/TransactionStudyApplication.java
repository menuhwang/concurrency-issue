package com.example.transactionstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
public class TransactionStudyApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(TransactionStudyApplication.class, args);
        BoardRepository boardRepository = applicationContext.getBean(BoardRepository.class);
        List<Board> defaultBoard = boardRepository.findAll();
        if (defaultBoard.isEmpty()) {
            boardRepository.save(
                    Board.builder()
                            .title("제목1")
                            .build()
            );
        } else {
            defaultBoard.forEach(board -> {
                board.resetLikes();
                boardRepository.save(board);
            });
        }
    }

}
