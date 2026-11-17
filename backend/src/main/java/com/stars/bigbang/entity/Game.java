package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "game")
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String slug;

    private LocalDate releaseDate;

    private String backgroundImage;

    @Column(unique = true)
    private Integer rawgId;

    @Column(unique = true)
    private Integer igdbId;

}
