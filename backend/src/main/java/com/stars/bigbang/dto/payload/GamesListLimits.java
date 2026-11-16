package com.stars.bigbang.dto.payload;

/** Limites communes aux payloads de listes, pour ne pas les répéter dans chaque DTO. */
public final class GamesListLimits {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int MAX_GAMES_PER_REQUEST = 200;

    private GamesListLimits() {
    }
}
