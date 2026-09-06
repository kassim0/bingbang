package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.GamesListEntry;

public record GamesListEntryDto(Long id, int position, GameDto game) {

    public static GamesListEntryDto from(GamesListEntry entry) {
        return new GamesListEntryDto(entry.getId(), entry.getPosition(), GameDto.from(entry.getGame()));
    }
}
