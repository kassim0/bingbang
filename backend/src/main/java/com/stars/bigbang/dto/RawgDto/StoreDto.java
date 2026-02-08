package com.stars.bigbang.dto.RawgDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StoreDto {

    private Integer id;
    private String name;
    private String slug;
    private String domain;

    @JsonProperty("games_count")
    private Integer gamesCount;

    @JsonProperty("image_background")
    private String imageBackground;

}
