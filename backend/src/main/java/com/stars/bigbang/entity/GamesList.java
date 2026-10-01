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
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="games_list_id")
    @OrderBy("position")
    private List<GamesListEntry> games;
}
