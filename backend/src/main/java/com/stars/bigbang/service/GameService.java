package com.stars.bigbang.service;

import com.stars.bigbang.dto.payload.UpdateGamesListDto;
import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.dto.response.GamesListDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GameSource;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GamesRepository gamesRepository;
    private final GamesListRepository gamesListRepository;
    private final GamesListEntryRepository gamesListEntryRepository;

    /**
     * Retrouve le jeu par l'identifiant de sa source, puis par son slug pour ne pas recréer
     * un jeu déjà connu via une autre source. Sinon, le crée.
     */
    private Game findOrCreateGame(GameSearchResult result) {
        return findByExternalId(result)
                .or(() -> findLinkableBySlug(result).map(game -> linkToSource(game, result)))
                .orElseGet(() -> createGame(result));
    }

    private Optional<Game> findByExternalId(GameSearchResult result) {
        return result.source() == GameSource.IGDB
                ? gamesRepository.findByIgdbId(result.externalId())
                : gamesRepository.findByRawgId(result.externalId());
    }

    /** Un jeu qui a déjà un autre identifiant sur cette source est un jeu différent, malgré le même slug. */
    private Optional<Game> findLinkableBySlug(GameSearchResult result) {
        if (result.slug() == null) {
            return Optional.empty();
        }
        return gamesRepository.findFirstBySlugOrderByIdAsc(result.slug())
                .filter(game -> (result.source() == GameSource.IGDB ? game.getIgdbId() : game.getRawgId()) == null);
    }

    private Game linkToSource(Game game, GameSearchResult result) {
        setExternalId(game, result);
        return gamesRepository.save(game);
    }

    private Game createGame(GameSearchResult result) {
        Game game = new Game();
        game.setName(result.name());
        game.setSlug(result.slug());
        game.setReleaseDate(result.releaseDate());
        game.setBackgroundImage(result.imageUrl());
        setExternalId(game, result);
        return gamesRepository.save(game);
    }

    private void setExternalId(Game game, GameSearchResult result) {
        if (result.source() == GameSource.IGDB) {
            game.setIgdbId(result.externalId());
        } else {
            game.setRawgId(result.externalId());
        }
    }

    /**
     * Create a new GamesList from search results
     * */
    public GamesList createGamesList(String listName, GameSearchResult[] games) {
        GamesList gamesList = new GamesList();
        List<Game> savedGames = new ArrayList<>();
        List<GamesListEntry>  savedGamesListEntry;

        for (GameSearchResult result : games) {
            savedGames.add(findOrCreateGame(result));
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
    public List<GamesListDto> getListGames(long userId) {

        return gamesListRepository.findAllByUserOrderByPosition(userId).stream()
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
        if(updateGamesListDto.newGames() != null && !updateGamesListDto.newGames().isEmpty()) {
            addGames(updateGamesListDto.gamesListId(),updateGamesListDto.newGames());
            returnMessage = "Games list updated";
        }
        return returnMessage;
    }

    private void addGames(long gamesListId, List<GameSearchResult> games) {
        GamesList gamesList = gamesListRepository.findById(gamesListId).orElseThrow();
        int nextPosition = gamesList.getGames().stream()
                .mapToInt(GamesListEntry::getPosition)
                .max()
                .orElse(1);
        for (GameSearchResult result : games) {
            gamesList.getGames().add(new GamesListEntry(findOrCreateGame(result), nextPosition++));
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

    public String deleteGamesList(Long gamesListId) {
        if(gamesListId == null) {
            return "ERROR : Nothing to delete";
        }
        gamesListRepository.deleteById(gamesListId);
        return "GamesList deleted";
    }
}
