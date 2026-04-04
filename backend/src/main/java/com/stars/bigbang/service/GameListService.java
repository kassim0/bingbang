package com.stars.bigbang.service;

import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GameList;
import com.stars.bigbang.repository.GameListRepository;
import com.stars.bigbang.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameListService {

    private final GameListRepository gameListRepository;
    private final GameRepository gameRepository;

    public GameList createList(String name, Long userId, String deviceId) {
        GameList gameList = new GameList();
        gameList.setName(name);
        gameList.setUserId(userId);
        gameList.setDeviceId(deviceId);
        return gameListRepository.save(gameList);
    }

    public List<GameList> findAll(Long userId, String deviceId) {
        if (userId != null) {
            return gameListRepository.findByUserId(userId);
        }
        if (deviceId != null) {
            return gameListRepository.findByDeviceId(deviceId);
        }
        return List.of();
    }

    public GameList addGameToList(Long listId, Game game) {
        GameList gameList = gameListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with id: " + listId));

        // Check if game already exists in DB by rawgId
        Optional<Game> existingGame = gameRepository.findByRawgId(game.getRawgId());
        Game savedGame = existingGame.orElseGet(() -> gameRepository.save(game));

        // Add game to list if not already present
        if (gameList.getGames().stream().noneMatch(g -> g.getId().equals(savedGame.getId()))) {
            gameList.getGames().add(savedGame);
            gameListRepository.save(gameList);
        }

        return gameList;
    }

}
