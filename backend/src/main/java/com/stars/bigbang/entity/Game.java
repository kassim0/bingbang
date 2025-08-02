package com.stars.bigbang.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Game {
    String name;
    String pegi;
    String Plateform;
    String[] categorie;
    LocalDateTime releaseDate;
    String note;
}
