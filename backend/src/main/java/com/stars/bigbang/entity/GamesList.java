package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class GamesList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Column(nullable = false)
    private int position;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="games_list_id")
    @OrderBy("position")
    private List<GamesListEntry> games;
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
}
