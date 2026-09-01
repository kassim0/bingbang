package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "games_list_entry")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GamesListEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "games_list_id")
    private GamesList gamesList;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(nullable = false)
    private int position;

    public GamesListEntry(GamesList gamesList, Game game, int position) {
        this.gamesList = gamesList;
        this.game = game;
        this.position = position;
    }
}
