package com.stars.bigbang.dto.record;

import com.stars.bigbang.dto.rawgDto.RawgResultsDto;

public record NewGamesList(String name, RawgResultsDto[] rawgGames) {
}
