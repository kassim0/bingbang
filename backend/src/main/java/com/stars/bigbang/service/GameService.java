package com.stars.bigbang.service;

import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.dto.record.UpdateGamesListDto;
import com.stars.bigbang.dto.response.GamesListDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GamesList;
import com.stars.bigbang.entity.GamesListEntry;
import com.stars.bigbang.repository.GamesListEntryRepository;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.GamesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GamesRepository gamesRepository;
    private final GamesListRepository gamesListRepository;
    private final GamesListEntryRepository gamesListEntryRepository;

    public Game saveRawgGame(RawgResultsDto gameDto) {
        Game game = new Game();
        game.setName(gameDto.name());
        game.setSlug(gameDto.slug());
        game.setBackgroundImage(gameDto.background_image());
        game.setRawgId(gameDto.id());
        return gamesRepository.save(game);
    }

    /**
     * Create a new GamesList from Rawg games
     * */
    public GamesList createGamesList(String listName, RawgResultsDto[] gameDto) {
        GamesList gamesList = new GamesList();
        List<Game> savedGames = new ArrayList<>();
        List<GamesListEntry>  savedGamesListEntry;

        for (RawgResultsDto dto : gameDto) {
            Game game = gamesRepository.findByRawgId(dto.id()).orElseGet(() -> saveRawgGame(dto));
            savedGames.add(game);
        }

        savedGamesListEntry = IntStream.range(0,savedGames.size())
                            .mapToObj(i-> new GamesListEntry(savedGames.get(i),i))
                            .toList();

        Long position = gamesListRepository.findMaxOrder() + 1;
        gamesList.setGames(savedGamesListEntry);
        gamesList.setPosition(position);
        gamesList.setName(listName.isEmpty() ? "Liste N° "+position : listName);
        return gamesListRepository.saveAndFlush(gamesList);
    }

    @Transactional(readOnly = true)
    public List<GamesListDto> getListGames() {

        return gamesListRepository.findAllWithGamesOrderByPosition().stream()
                .map(GamesListDto::from)
                .toList();
    }

    @Transactional
    public String updateGamesList(UpdateGamesListDto updateGamesListDto) {
        String returnMessage = "Nothing to update";
        if(updateGamesListDto.newName() != null) {
            gamesListRepository.updateNameById(updateGamesListDto.gamesListId(), updateGamesListDto.newName());
            returnMessage = "Games list updated";
        }
        if(updateGamesListDto.removeGameId() != null && !updateGamesListDto.removeGameId().isEmpty()) {
            gamesListEntryRepository.deleteGameByGamesListId(updateGamesListDto.gamesListId(),updateGamesListDto.removeGameId());
            manageGamesListEntryPosition(updateGamesListDto.gamesListId());
            returnMessage = "Games list updated";
        }
        if(updateGamesListDto.newGameId() != null && !updateGamesListDto.newGameId().isEmpty()) {
            addGames(updateGamesListDto.gamesListId(),updateGamesListDto.newGameId());
            returnMessage = "Games list updated";
        }
        return returnMessage;
    }

    private void addGames(long gamesListId, List<Long> gameIds) {
        GamesList gamesList = gamesListRepository.findById(gamesListId).orElseThrow();
        int nextPosition = gamesList.getGames().stream()
                .mapToInt(GamesListEntry::getPosition)
                .max()
                .orElse(1);
        for (Long gameId : gameIds) {
            Game game = gamesRepository.findById(gameId).orElseThrow();
            gamesList.getGames().add(new GamesListEntry(game, nextPosition++));
        }
        gamesListRepository.save(gamesList);
    }

    private void manageGamesListEntryPosition(long gamesListId) {
        AtomicInteger compteur = new AtomicInteger();
        List<GamesListEntry> gamesListEntryList = gamesListEntryRepository.findByGamesListId(gamesListId).stream()
                .sorted(Comparator.comparingInt(GamesListEntry::getPosition))
                .toList();

        gamesListEntryList.forEach(gamesListEntry -> {
            if(gamesListEntry.getPosition() != compteur.getAndIncrement()) {
                gamesListEntry.setPosition(compteur.get());
            }
        });

        gamesListEntryRepository.saveAll(gamesListEntryList);
    }
}
