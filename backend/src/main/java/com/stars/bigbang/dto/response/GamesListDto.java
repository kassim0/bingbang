package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.GamesList;

import java.util.List;

public record GamesListDto(int id, String name, Long position, List<GamesListEntryDto> games) {

    public static GamesListDto from(GamesList gamesList) {
        List<GamesListEntryDto> games = gamesList.getGames().stream()
                .map(GamesListEntryDto::from)
                .toList();
        return new GamesListDto(gamesList.getId(), gamesList.getName(), gamesList.getPosition(), games);
    }
}
