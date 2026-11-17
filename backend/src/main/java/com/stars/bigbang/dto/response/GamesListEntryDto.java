package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.GamesListEntry;

/**
 * @param commentary commentaire de l'utilisateur sur ce jeu dans cette liste, null s'il n'y en a pas
 */
public record GamesListEntryDto(Long id, int position, GameDto game, String commentary) {

    public static GamesListEntryDto from(GamesListEntry entry) {
        return new GamesListEntryDto(entry.getId(), entry.getPosition(), GameDto.from(entry.getGame()), null);
    }

    public GamesListEntryDto withCommentary(String commentary) {
        return new GamesListEntryDto(id, position, game, commentary);
    }
}
