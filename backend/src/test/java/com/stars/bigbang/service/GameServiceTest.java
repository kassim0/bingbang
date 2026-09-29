package com.stars.bigbang.service;

import com.stars.bigbang.dto.record.UpdateGamesListDto;
import com.stars.bigbang.entity.GamesList;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.GamesListEntryRepository;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.GamesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GamesRepository gamesRepository;
    @Mock
    private GamesListRepository gamesListRepository;
    @Mock
    private GamesListEntryRepository gamesListEntryRepository;

    private GameService gameService() {
        return new GameService(gamesRepository, gamesListRepository, gamesListEntryRepository);
    }

    @Test
    void updateGamesListRejectsWhenListDoesNotBelongToUser() {
        GameService gameService = gameService();
        User otherUser = new User("bob", "bob@example.com", "hashed");
        UpdateGamesListDto dto = new UpdateGamesListDto(42L, null, null, "New name");
        when(gamesListRepository.findByIdAndUser(42L, otherUser)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameService.updateGamesList(dto, otherUser));

        assertEquals(404, ex.getStatusCode().value());
        verify(gamesListRepository, never()).updateNameById(anyLong(), any());
    }

    @Test
    void updateGamesListRenamesWhenOwnedByUser() {
        GameService gameService = gameService();
        User owner = new User("alice", "alice@example.com", "hashed");
        UpdateGamesListDto dto = new UpdateGamesListDto(42L, null, null, "New name");
        when(gamesListRepository.findByIdAndUser(42L, owner)).thenReturn(Optional.of(new GamesList()));

        String result = gameService.updateGamesList(dto, owner);

        assertEquals("Games list updated", result);
        verify(gamesListRepository).updateNameById(42L, "New name");
    }

    @Test
    void deleteGamesListRejectsWhenListDoesNotBelongToUser() {
        GameService gameService = gameService();
        User otherUser = new User("bob", "bob@example.com", "hashed");
        when(gamesListRepository.findByIdAndUser(42L, otherUser)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameService.deleteGamesList(42L, otherUser));

        assertEquals(404, ex.getStatusCode().value());
        verify(gamesListRepository, never()).delete(any());
    }

    @Test
    void deleteGamesListDeletesWhenOwnedByUser() {
        GameService gameService = gameService();
        User owner = new User("alice", "alice@example.com", "hashed");
        GamesList gamesList = new GamesList();
        when(gamesListRepository.findByIdAndUser(42L, owner)).thenReturn(Optional.of(gamesList));

        String result = gameService.deleteGamesList(42L, owner);

        assertEquals("GamesList deleted", result);
        verify(gamesListRepository).delete(gamesList);
    }

    @Test
    void getListGamesOnlyReturnsListsForGivenUser() {
        GameService gameService = gameService();
        User owner = new User("alice", "alice@example.com", "hashed");
        when(gamesListRepository.findAllWithGamesOrderByPositionAndUser(owner)).thenReturn(List.of());

        assertEquals(0, gameService.getListGames(owner).size());
    }
}
