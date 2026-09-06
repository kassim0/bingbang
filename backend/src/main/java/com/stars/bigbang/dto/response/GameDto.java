package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.Game;

import java.time.LocalDate;

public record GameDto(
        Long id,
        String name,
        String slug,
        LocalDate releaseDate,
        String backgroundImage,
        Integer rawgId
) {
    public static GameDto from(Game game) {
        return new GameDto(
                game.getId(),
                game.getName(),
                game.getSlug(),
                game.getReleaseDate(),
                game.getBackgroundImage(),
                game.getRawgId()
        );
    }
}
