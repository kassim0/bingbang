package com.stars.bigbang.service;

import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.dto.record.UpdateGamesListDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GamesList;
import com.stars.bigbang.entity.GamesListEntry;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.GamesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GamesRepository gamesRepository;
    private final GamesListRepository gamesListRepository;

    public Game saveRawgGame(RawgResultsDto gameDto) {
        Game game = new Game();
        game.setName(gameDto.getName());
        game.setSlug(gameDto.getSlug());
        game.setBackgroundImage(gameDto.getBackground_image());
        game.setRawgId(gameDto.getId());
        return gamesRepository.save(game);
    }

    public List<Game> findAll() {
        return gamesRepository.findAll();
    }

    public Optional<Game> findById(Long id) {
        return gamesRepository.findById(id);
    }

    public Optional<Game> findByRawgId(Integer rawgId) {
        return gamesRepository.findByRawgId(rawgId);
    }

    public Optional<Game> findBySlug(String slug) {
        return gamesRepository.findBySlug(slug);
    }

    public void deleteById(Long id) {
        gamesRepository.deleteById(id);
    }

    /**
     * Create a new GamesList from Rawg games
     * */
    public GamesList createGamesList(String listName, RawgResultsDto[] gameDto) {
        GamesList gamesList = new GamesList();
        List<Game> savedGames = new ArrayList<>();
        List<GamesListEntry>  savedGamesListEntry = new ArrayList<>();

        for (RawgResultsDto dto : gameDto) {
            Game game = gamesRepository.findByRawgId(dto.getId()).orElseGet(() -> {
                Game newGame = new Game();
                newGame.setName(dto.getName());
                newGame.setSlug(dto.getSlug());
                newGame.setBackgroundImage(dto.getBackground_image());
                newGame.setRawgId(dto.getId());
                return gamesRepository.save(newGame);
            });
            savedGames.add(game);
        }

        savedGamesListEntry = IntStream.range(0,savedGames.size())
                            .mapToObj(i-> new GamesListEntry(gamesList,savedGames.get(i),i))
                            .toList();

        Long position = gamesListRepository.findMaxOrder() + 1;
        gamesList.setGames(savedGamesListEntry);
        gamesList.setPosition(position);
        gamesList.setName(listName.isEmpty() ? "Liste N° "+position : listName);
        return gamesListRepository.saveAndFlush(gamesList);
    }

    public List<GamesList> getListGames() {
        return gamesListRepository.findAll().stream()
                .filter(gamesList -> gamesList.getPosition() != null)
                .sorted(Comparator.comparing(GamesList::getPosition))
                .toList();
    }

    public void updateGamesList(UpdateGamesListDto updateGamesListDto) {
        if(updateGamesListDto.newName() != null) {
            gamesListRepository.updateNameById(updateGamesListDto.gamesListId(), updateGamesListDto.newName());
        }
        if(updateGamesListDto.removeGameId() != null) {
            gamesListRepository.deleteGame(updateGamesListDto.gamesListId(),updateGamesListDto.removeGameId());
        }
        if(updateGamesListDto.newGameId() != null) {
            gamesListRepository.saveGame(updateGamesListDto.gamesListId(),updateGamesListDto.newGameId().toArray(Long[]::new));
        }
    }
}
