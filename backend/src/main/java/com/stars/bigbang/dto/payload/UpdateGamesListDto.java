package com.stars.bigbang.dto.payload;

import com.stars.bigbang.dto.response.GameSearchResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Chaque champ est facultatif : null signifie "ne pas toucher à cette partie de la liste".
 *
 * @param newName nouveau nom ; s'il est fourni, il doit contenir au moins un caractère visible
 */
public record UpdateGamesListDto(
        long gamesListId,
        @Size(max = GamesListLimits.MAX_GAMES_PER_REQUEST) List<@Valid @NotNull GameSearchResult> newGames,
        @Size(max = GamesListLimits.MAX_GAMES_PER_REQUEST) List<@NotNull Long> removeGameId,
        @Size(max = GamesListLimits.NAME_MAX_LENGTH) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String newName
) {
}
