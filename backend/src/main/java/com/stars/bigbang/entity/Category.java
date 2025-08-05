package com.stars.bigbang.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@Entity
@Table(name="category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String category;

    @ManyToMany(mappedBy = "categories")
    private Set<Game> game = new HashSet<>();

    public Category() {}

    public Category(String Category){
        this.category = Category;
    }
}
