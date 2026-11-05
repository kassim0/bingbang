package com.stars.bigbang.dto.igdbDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @param first_release_date timestamp Unix en secondes, absent si le jeu n'a pas de date connue
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IgdbGameDto(int id, String slug, String name, Long first_release_date, IgdbCoverDto cover) {
}
