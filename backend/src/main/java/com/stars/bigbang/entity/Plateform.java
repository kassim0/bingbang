package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="plateform")
public class Plateform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plateform;

    @ManyToMany(mappedBy = "plateforms")
    private Set<Game> game = new HashSet<>();
}
