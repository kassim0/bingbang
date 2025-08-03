package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
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

}
