package com.stars.bigbang.dto.record;

import com.stars.bigbang.dto.rawgDto.RawgResultsDto;

import java.util.List;

public record UpdateGamesListDto(long gamesListId, List<RawgResultsDto> newRawgGames, List<Long> removeGameId, String newName) {
}
