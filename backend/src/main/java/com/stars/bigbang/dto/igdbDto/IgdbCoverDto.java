package com.stars.bigbang.dto.igdbDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IgdbCoverDto(String image_id) {
}
