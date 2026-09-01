package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
public class GamesList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private Long position;
    @OneToMany(mappedBy = "gamesList", cascade = CascadeType.ALL)
    private List<GamesListEntry> games;
}
