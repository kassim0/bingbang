package com.stars.bigbang.dto.rawgDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawgResponseDto {

    private int count;
    private String next;
    private String previous;
    private List<RawgResultsDto> results;
    private String seo_title;
    private String seo_keywords;
    private String seo_h1;
    private boolean noindex;
    private boolean nofollow;
    private String description;
    private Object filters;
    private Object nofollow_collections;
}
