package com.stars.bigbang.mock;

import com.stars.bigbang.dto.igdbDto.IgdbGameDto;
import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockCatalogTest {

    private static final String IGDB_QUERY = """
            search "%s";
            fields name,slug,first_release_date,cover.image_id;
            limit 20;
            """;

    private final IgdbApiMock igdbApiMock = new IgdbApiMock();
    private final RawgApiMock rawgApiMock = new RawgApiMock();

    @Test
    void igdbMockOnlyReturnsGamesMatchingTheSearchedText() {
        List<IgdbGameDto> games = igdbApiMock.searchGames("client", "Bearer token", IGDB_QUERY.formatted("zelda"));

        assertFalse(games.isEmpty());
        assertTrue(games.stream().allMatch(game -> game.name().toLowerCase().contains("zelda")));
    }

    @Test
    void twoDifferentSearchesDoNotReturnTheSameFirstGame() {
        IgdbGameDto zelda = igdbApiMock.searchGames("client", "Bearer token", IGDB_QUERY.formatted("zelda")).get(0);
        IgdbGameDto mario = igdbApiMock.searchGames("client", "Bearer token", IGDB_QUERY.formatted("mario")).get(0);

        assertFalse(zelda.id() == mario.id());
    }

    @Test
    void searchIgnoresCaseAndSurroundingSpaces() {
        assertEquals(1, MockCatalog.search("  OUTER wilds ").size());
    }

    @Test
    void unknownGameReturnsNoResult() {
        RawgResponseDto response = rawgApiMock.searchGamesByName("a game that does not exist");

        assertEquals(0, response.getCount());
        assertTrue(response.getResults().isEmpty());
    }
}
