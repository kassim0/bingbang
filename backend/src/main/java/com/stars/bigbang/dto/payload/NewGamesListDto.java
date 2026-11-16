package com.stars.bigbang.dto.payload;

import com.stars.bigbang.dto.response.GameSearchResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * @param name  nom de la liste, facultatif : un nom par défaut est donné s'il est absent
 * @param games jeux à mettre dans la liste, facultatif (null = aucun jeu)
 */
public record NewGamesListDto(
        @Size(max = GamesListLimits.NAME_MAX_LENGTH) String name,
        // @Valid devant un tableau : chaque jeu du tableau est validé à son tour
        @Valid @Size(max = GamesListLimits.MAX_GAMES_PER_REQUEST) GameSearchResult[] games
) {
}
