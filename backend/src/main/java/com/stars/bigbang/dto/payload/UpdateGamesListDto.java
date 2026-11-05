package com.stars.bigbang.dto.payload;

import com.stars.bigbang.dto.response.GameSearchResult;

import java.util.List;

public record UpdateGamesListDto(long gamesListId, List<GameSearchResult> newGames, List<Long> removeGameId, String newName) {
}
