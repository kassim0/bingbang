package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    String name;
    String pegi;
    String Plateform;
    String categorie;
    LocalDateTime releaseDate;
    String note;
}
