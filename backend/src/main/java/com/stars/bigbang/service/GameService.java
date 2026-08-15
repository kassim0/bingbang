package com.stars.bigbang.service;

import com.stars.bigbang.dto.RawgDto.RawgResultsDto;
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
        Game[] games = new Game[gameDto.length];
        for (int i = 0; i < gameDto.length; i++) {
            games[i] = new Game();
            games[i].setName(gameDto[i].getName());
            games[i].setSlug(gameDto[i].getSlug());
            games[i].setBackgroundImage(gameDto[i].getBackground_image());
            games[i].setRawgId(gameDto[i].getId());
        }
        gameList.setListGames(List.of(games));
        return gameListRepository.saveAndFlush(gameList);
    }
}
