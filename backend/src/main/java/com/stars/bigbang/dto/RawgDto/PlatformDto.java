package com.stars.bigbang.dto.RawgDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformDto {

    private Integer id;
    private String name;
    private String slug;
    private String image;

    @JsonProperty("year_end")
    private Integer yearEnd;

    @JsonProperty("year_start")
    private Integer yearStart;

    @JsonProperty("games_count")
    private Integer gamesCount;

    @JsonProperty("image_background")
    private String imageBackground;

}
