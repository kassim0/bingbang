package com.stars.bigbang.dto.RawgDto;

import lombok.Data;

import java.util.List;

@Data
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
    private String filters; //TODO modifier la nature de l'attribut
    private String nofollow_collections; //TODO modifier la nature de l'attribut
}
