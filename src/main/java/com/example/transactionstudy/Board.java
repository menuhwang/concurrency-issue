package com.example.transactionstudy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@ToString
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private int likes;
    @Version
    @JsonIgnore
    private LocalDateTime version;

    protected Board() {
    }

    @Builder
    public Board(int id, String title, int likes) {
        this.id = id;
        this.title = title;
        this.likes = likes;
    }

    public void like() {
        this.likes++;
    }

    public void resetLikes() {
        this.likes = 0;
    }
}
