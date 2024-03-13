package com.stars.bigbang.dto;

import lombok.Data;

@Data
public class RawgTagDto {

    private int id;
    private String name;
    private String slug;
    private String language;
    private int games_count;
    private String image_background;
}
