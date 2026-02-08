package com.stars.bigbang.dto.RawgDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class RawgResultsDto {

    private Integer id;
    private String slug;
    private String name;
    private String released;
    private Boolean tba;

    @JsonProperty("background_image")
    private String background_image;

    private Double rating;

    @JsonProperty("rating_top")
    private Integer ratingTop;

    @JsonProperty("ratings_count")
    private Integer ratingsCount;

    @JsonProperty("reviews_text_count")
    private Integer reviewsTextCount;

    private Integer added;
    private Integer metacritic;
    private Integer playtime;

    @JsonProperty("suggestions_count")
    private Integer suggestionsCount;

    private String updated;

    @JsonProperty("reviews_count")
    private Integer reviewsCount;

    @JsonProperty("saturated_color")
    private String saturatedColor;

    @JsonProperty("dominant_color")
    private String dominantColor;

    private List<PlatformWrapperDto> platforms;
    private List<GenreDto> genres;
    private List<StoreWrapperDto> stores;
    private List<TagDto> tags;

    @JsonProperty("esrb_rating")
    private EsrbRatingDto esrbRating;

    @JsonProperty("short_screenshots")
    private List<ShortScreenshotDto> shortScreenshots;

}
