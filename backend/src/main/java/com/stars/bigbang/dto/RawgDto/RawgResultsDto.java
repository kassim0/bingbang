package com.stars.bigbang.dto.RawgDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawgResultsDto {

    private int id;
    private String slug;
    private String name;
    private String released;
    private boolean tba;
    private String background_image;

}
