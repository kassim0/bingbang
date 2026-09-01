package com.stars.bigbang.dto.record;

import java.util.List;

public record UpdateGamesListDto(long gamesListId, List<Long> newGameId, List<Long> removeGameId, String newName) {
}
