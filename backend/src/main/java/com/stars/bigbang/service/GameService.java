package com.stars.bigbang.service;

import com.stars.bigbang.dto.RawgDto.RawgResultsDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GameList;
import com.stars.bigbang.repository.GameListRepository;
import com.stars.bigbang.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameListRepository gameListRepository;

    public Game saveRawgGame(RawgResultsDto gameDto) {
        Game game = new Game();
        game.setName(gameDto.getName());
        game.setSlug(gameDto.getSlug());
        game.setBackgroundImage(gameDto.getBackground_image());
        game.setRawgId(gameDto.getId());
        return gameRepository.save(game);
    }

    public List<Game> findAll() {
        return gameRepository.findAll();
    }

    public Optional<Game> findById(Long id) {
        return gameRepository.findById(id);
    }

    public Optional<Game> findByRawgId(Integer rawgId) {
        return gameRepository.findByRawgId(rawgId);
    }

    public Optional<Game> findBySlug(String slug) {
        return gameRepository.findBySlug(slug);
    }

    public void deleteById(Long id) {
        gameRepository.deleteById(id);
    }

    public GameList saveRawgListGame(RawgResultsDto[] gameDto) {
        GameList gameList = new GameList();
        List<Game> savedGames = new ArrayList<>();
        for (RawgResultsDto dto : gameDto) {
            Game game = gameRepository.findByRawgId(dto.getId()).orElseGet(() -> {
                Game newGame = new Game();
                newGame.setName(dto.getName());
                newGame.setSlug(dto.getSlug());
                newGame.setBackgroundImage(dto.getBackground_image());
                newGame.setRawgId(dto.getId());
                return gameRepository.save(newGame);
            });
            savedGames.add(game);
        }
        Long listOrder = gameListRepository.findMaxListOrder() + 1;
        gameList.setListGames(savedGames);
        gameList.setListOrder(listOrder);
        gameList.setListName("Liste N° "+listOrder);
        return gameListRepository.saveAndFlush(gameList);
    }
}
