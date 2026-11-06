package com.stars.bigbang.service;

import com.stars.bigbang.dto.payload.NewGamesListDto;
import com.stars.bigbang.dto.payload.UpdateGamesListDto;
import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.entity.GamesList;
import com.stars.bigbang.entity.GamesListEntry;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.GamesListEntryRepository;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.GamesRepository;
import com.stars.bigbang.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GamesRepository gamesRepository;
    @Mock
    private GamesListRepository gamesListRepository;
    @Mock
    private GamesListEntryRepository gamesListEntryRepository;
    @Mock
    private UserRepository userRepository;

    private static final Long USER_ID = 1L;
    private static final long LIST_ID = 10L;

    private GameService gameService;
    private User user;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gamesRepository, gamesListRepository, gamesListEntryRepository, userRepository);
        user = new User();
        // lenient : ces mocks ne servent qu'aux tests de création, pas à ceux de modification/suppression
        lenient().when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        lenient().when(gamesListRepository.findMaxOrder(USER_ID)).thenReturn(0L);
        lenient().when(gamesListRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(gamesRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private Game savedGame(GameSearchResult result) {
        GamesList list = gameService.createGamesList(new NewGamesListDto("liste", new GameSearchResult[]{result}), USER_ID);
        return list.getGames().get(0).getGame();
    }

    private Game rawgGame(int rawgId, String slug) {
        Game game = new Game();
        game.setRawgId(rawgId);
        game.setSlug(slug);
        return game;
    }

    @Test
    void createdGamesListBelongsToTheConnectedUser() {
        GamesList list = gameService.createGamesList(new NewGamesListDto("liste", new GameSearchResult[]{}), USER_ID);

        assertSame(user, list.getUser());
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

    // --- Propriété : un utilisateur ne peut pas toucher à la liste d'un autre ---

    @Test
    void deletingAGamesListOfAnotherUserThrowsNotFound() {
        when(gamesListRepository.deleteGamesListByIdAndUserId(LIST_ID, USER_ID)).thenReturn(0);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> gameService.deleteGamesList(LIST_ID, USER_ID));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deletingOwnGamesListSucceeds() {
        when(gamesListRepository.deleteGamesListByIdAndUserId(LIST_ID, USER_ID)).thenReturn(1);

        assertDoesNotThrow(() -> gameService.deleteGamesList(LIST_ID, USER_ID));
    }

    @Test
    void updatingAGamesListOfAnotherUserThrowsNotFoundAndChangesNothing() {
        when(gamesListRepository.existsByIdAndUserId(LIST_ID, USER_ID)).thenReturn(false);
        UpdateGamesListDto update = new UpdateGamesListDto(LIST_ID, List.of(rawgResult(1)), List.of(5L), "nouveau nom");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> gameService.updateGamesList(update, USER_ID));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(gamesListRepository, never()).updateNameById(anyLong(), any());
        verify(gamesListRepository, never()).findById(any());
        verifyNoInteractions(gamesListEntryRepository);
    }

    // --- Numérotation des jeux dans une liste ---

    @Test
    void removingGamesRenumbersRemainingGamesWithoutGaps() {
        when(gamesListRepository.existsByIdAndUserId(LIST_ID, USER_ID)).thenReturn(true);
        when(gamesListEntryRepository.deleteGameByGamesListId(LIST_ID, List.of(5L))).thenReturn(1);
        GamesListEntry first = entryAt(0);
        GamesListEntry second = entryAt(2);
        GamesListEntry third = entryAt(3);
        when(gamesListEntryRepository.findByGamesListId(LIST_ID)).thenReturn(List.of(third, first, second));

        gameService.updateGamesList(new UpdateGamesListDto(LIST_ID, null, List.of(5L), null), USER_ID);

        assertEquals(0, first.getPosition());
        assertEquals(1, second.getPosition());
        assertEquals(2, third.getPosition());
    }

    @Test
    void addingGamesToAnEmptyListStartsAtPositionZero() {
        GamesList gamesList = listWithGames();

        gameService.updateGamesList(new UpdateGamesListDto(LIST_ID, List.of(rawgResult(1), rawgResult(2)), null, null), USER_ID);

        assertEquals(List.of(0, 1), positionsOf(gamesList));
    }

    @Test
    void addedGamesFollowTheLastGameOfTheList() {
        GamesList gamesList = listWithGames(entryAt(0), entryAt(1));

        gameService.updateGamesList(new UpdateGamesListDto(LIST_ID, List.of(rawgResult(1)), null, null), USER_ID);

        assertEquals(List.of(0, 1, 2), positionsOf(gamesList));
    }

    private GameSearchResult rawgResult(int rawgId) {
        return new GameSearchResult(GameSource.RAWG, rawgId, null, "Jeu " + rawgId, null, "img");
    }

    private GamesListEntry entryAt(int position) {
        return new GamesListEntry(new Game(), position);
    }

    /** Liste appartenant à USER_ID, renvoyée par findById, contenant les entrées données. */
    private GamesList listWithGames(GamesListEntry... entries) {
        when(gamesListRepository.existsByIdAndUserId(LIST_ID, USER_ID)).thenReturn(true);
        GamesList gamesList = new GamesList();
        gamesList.setGames(new ArrayList<>(List.of(entries)));
        when(gamesListRepository.findById(LIST_ID)).thenReturn(Optional.of(gamesList));
        return gamesList;
    }

    private List<Integer> positionsOf(GamesList gamesList) {
        return gamesList.getGames().stream().map(GamesListEntry::getPosition).toList();
    }
}
