package com.stars.bigbang.dto.payload;

import com.stars.bigbang.dto.response.GameSearchResult;

public record NewGamesListDto(String name, GameSearchResult[] games) {
}
