package com.stars.bigbang.service;

import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.entity.GamesList;
import com.stars.bigbang.repository.GamesListEntryRepository;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.GamesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GamesRepository gamesRepository;
    @Mock
    private GamesListRepository gamesListRepository;
    @Mock
    private GamesListEntryRepository gamesListEntryRepository;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gamesRepository, gamesListRepository, gamesListEntryRepository);
        when(gamesListRepository.findMaxOrder()).thenReturn(0L);
        when(gamesListRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(gamesRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private Game savedGame(GameSearchResult result) {
        GamesList list = gameService.createGamesList("liste", new GameSearchResult[]{result});
        return list.getGames().get(0).getGame();
    }

    private Game rawgGame(int rawgId, String slug) {
        Game game = new Game();
        game.setRawgId(rawgId);
        game.setSlug(slug);
        return game;
    }

    @Test
    void igdbIdIdenticalToRawgIdOfAnotherGameDoesNotReuseThatGame() {
        when(gamesRepository.findByIgdbId(4200)).thenReturn(Optional.empty());
        when(gamesRepository.findFirstBySlugOrderByIdAsc("zelda")).thenReturn(Optional.empty());

        Game game = savedGame(new GameSearchResult(GameSource.IGDB, 4200, "zelda", "Zelda", null, "img"));

        assertEquals(4200, game.getIgdbId());
        assertNull(game.getRawgId());
        assertEquals("Zelda", game.getName());
        verify(gamesRepository, never()).findByRawgId(any());
    }

    @Test
    void sameSlugLinksIgdbIdToExistingRawgGameWithoutDuplicate() {
        Game existing = rawgGame(3498, "portal-2");
        when(gamesRepository.findByIgdbId(72)).thenReturn(Optional.empty());
        when(gamesRepository.findFirstBySlugOrderByIdAsc("portal-2")).thenReturn(Optional.of(existing));

        Game game = savedGame(new GameSearchResult(GameSource.IGDB, 72, "portal-2", "Portal 2", null, "img"));

        assertSame(existing, game);
        assertEquals(72, game.getIgdbId());
        assertEquals(3498, game.getRawgId());
    }

    @Test
    void sameSlugWithAnotherIdOnSameSourceCreatesNewGame() {
        Game existing = rawgGame(3498, "portal-2");
        existing.setIgdbId(1);
        when(gamesRepository.findByIgdbId(72)).thenReturn(Optional.empty());
        when(gamesRepository.findFirstBySlugOrderByIdAsc("portal-2")).thenReturn(Optional.of(existing));

        Game game = savedGame(new GameSearchResult(GameSource.IGDB, 72, "portal-2", "Portal 2", null, "img"));

        assertNotSame(existing, game);
        assertEquals(72, game.getIgdbId());
        assertEquals(1, existing.getIgdbId());
    }

    @Test
    void gameAlreadyKnownOnItsSourceIsReused() {
        Game existing = rawgGame(3498, "portal-2");
        when(gamesRepository.findByRawgId(3498)).thenReturn(Optional.of(existing));

        Game game = savedGame(new GameSearchResult(GameSource.RAWG, 3498, "portal-2", "Portal 2", null, "img"));

        assertSame(existing, game);
        verify(gamesRepository, never()).save(any());
    }
}
