package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@Entity
@Table(name="game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private String pegi;

    @ManyToMany
    private Set<Plateform> plateforms = new HashSet<>();

    @ManyToMany
    private Set<Category> categories = new HashSet<>();

    private LocalDateTime releaseDate;

    public Game(){}

    public Game(String name, Set<Plateform> plateforms, Set<Category> categories) {
        this.name = name;
        this.plateforms = plateforms;
        this.categories = categories;
    }

    public Game(String name, Set<Plateform> plateforms, Set<Category> categories, LocalDateTime releaseDate) {
        this.name = name;
        this.plateforms = plateforms;
        this.categories = categories;
        this.releaseDate = releaseDate;
    }

    public Game(String name, String pegi, Set<Plateform> plateforms, Set<Category> categories) {
        this.name = name;
        this.pegi = pegi;
        this.plateforms = plateforms;
        this.categories = categories;
    }

    public Game(String name, String pegi, Set<Plateform> plateforms, Set<Category> categories, LocalDateTime releaseDate) {
        this.name = name;
        this.pegi = pegi;
        this.plateforms = plateforms;
        this.categories = categories;
        this.releaseDate = releaseDate;
    }
}
