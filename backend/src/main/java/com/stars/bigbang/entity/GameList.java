package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class GameList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToMany
    private List<Game> listGames;

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long listOrder;

    private String listName;
}
