package com.stars.bigbang.rest.dto.RawgDto;

import lombok.Data;

@Data
public class RawgRatingDto {
    private int id;
    private String title;
    private int count;
    private double percent;
}
