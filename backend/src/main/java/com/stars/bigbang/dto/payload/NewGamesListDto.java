package com.stars.bigbang.dto.payload;

import com.stars.bigbang.dto.rawgDto.RawgResultsDto;

public record NewGamesListDto(String name, RawgResultsDto[] rawgGames) {
}
