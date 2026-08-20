package com.stars.bigbang.dto.record;

import com.stars.bigbang.dto.rawgDto.RawgResultsDto;

public record NewGameList(String listName, RawgResultsDto[] gameDto) {
}
