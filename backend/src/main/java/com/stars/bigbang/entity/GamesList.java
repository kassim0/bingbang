package com.stars.bigbang.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private int id;
    private String name;
    private Long position;
    @OneToMany(mappedBy = "gamesList", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<GamesListEntry> games;
}
