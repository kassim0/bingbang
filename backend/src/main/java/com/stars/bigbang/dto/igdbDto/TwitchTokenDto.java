package com.stars.bigbang.dto.igdbDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @param expires_in durée de validité du token, en secondes
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TwitchTokenDto(String access_token, long expires_in) {
}
