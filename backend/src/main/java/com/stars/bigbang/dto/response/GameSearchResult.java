package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.GameSource;

import java.time.LocalDate;

/**
 * Résultat de recherche commun à toutes les sources.
 * Un externalId n'a de sens qu'avec sa source : le même nombre peut désigner deux jeux différents.
 */
public record GameSearchResult(
        GameSource source,
        Integer externalId,
        String slug,
        String name,
        LocalDate releaseDate,
        String imageUrl
) {
}
