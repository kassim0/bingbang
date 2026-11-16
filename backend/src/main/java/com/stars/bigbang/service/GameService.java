package com.stars.bigbang.service;

import com.stars.bigbang.dto.payload.NewGamesListDto;
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
import com.stars.bigbang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GamesRepository gamesRepository;
    private final GamesListRepository gamesListRepository;
    private final GamesListEntryRepository gamesListEntryRepository;
    private final UserRepository userRepository;

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
        game.setBackgroundImage(ImageUrlPolicy.trustedOrNull(result.imageUrl()));
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
     * Crée une nouvelle liste à partir de résultats de recherche.
     * Un jeu présent plusieurs fois dans la demande n'est ajouté qu'une fois.
     */
    @Transactional
    public GamesList createGamesList(NewGamesListDto newGamesListDto, Long userId) {
        GamesList gamesList = new GamesList();
        List<Game> savedGames = new ArrayList<>();
        String listName = newGamesListDto.name() == null ? "" : newGamesListDto.name().strip();
        GameSearchResult[] games = newGamesListDto.games() == null ? new GameSearchResult[0] : newGamesListDto.games();

        for (GameSearchResult result : games) {
            Game game = findOrCreateGame(result);
            if (!containsGame(savedGames, game)) {
                savedGames.add(game);
            }
        }

        // ArrayList et non toList() : Hibernate doit pouvoir modifier cette liste par la suite
        List<GamesListEntry> savedGamesListEntry = new ArrayList<>();
        for (int position = 0; position < savedGames.size(); position++) {
            savedGamesListEntry.add(new GamesListEntry(savedGames.get(position), position));
        }

        Long position = gamesListRepository.findMaxOrder(userId) + 1;
        gamesList.setGames(savedGamesListEntry);
        gamesList.setPosition(position);
        gamesList.setName(listName.isEmpty() ? "Liste N° " + position : listName);
        gamesList.setUser(userRepository.getReferenceById(userId));
        return gamesListRepository.saveAndFlush(gamesList);
    }

    /**
     * Vrai si le jeu est déjà dans la liste donnée. On compare les identifiants de base de données ;
     * la comparaison d'instance (==) couvre le cas d'un jeu pas encore enregistré.
     */
    private boolean containsGame(List<Game> games, Game game) {
        return games.stream().anyMatch(known -> known == game || (known.getId() != null && known.getId().equals(game.getId())));
    }

    @Transactional(readOnly = true)
    public List<GamesListDto> getGamesList(long userId) {

        return gamesListRepository.findAllByUserOrderByPosition(userId).stream()
                .map(GamesListDto::from)
                .toList();
    }

    @Transactional
    public String updateGamesList(UpdateGamesListDto updateGamesListDto, long userId) {
        if (!gamesListRepository.existsByIdAndUserId(updateGamesListDto.gamesListId(), userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Games list not found");
        }
        String returnMessage = "Nothing to update";
        boolean nameUpdated = updateNameGamesList(updateGamesListDto.gamesListId(), updateGamesListDto.newName());
        boolean gamesRemoved = removeGamesFromGamesList(updateGamesListDto.gamesListId(), updateGamesListDto.removeGameId());
        boolean gamesAdded = addGamesInGamesList(updateGamesListDto.gamesListId(), updateGamesListDto.newGames());

        if (nameUpdated || gamesRemoved || gamesAdded) {
            returnMessage = "Games list updated";
        }
        return returnMessage;
    }

    private boolean updateNameGamesList(long gamesListId, String gamesListName) {
        boolean nameUpdated = false;
        if (gamesListName != null) {
            gamesListRepository.updateNameById(gamesListId, gamesListName.strip());
            nameUpdated = true;
        }
        return nameUpdated;
    }

    private boolean removeGamesFromGamesList(long gamesListId, List<Long> gamesIds) {
        int gamesRemoved = 0;
        if (gamesIds != null && !gamesIds.isEmpty()) {
            gamesRemoved = gamesListEntryRepository.deleteGameByGamesListId(gamesListId, gamesIds);
            manageGamesListEntryPosition(gamesListId);
        }
        return gamesRemoved > 0;
    }

    /** Ajoute les jeux à la fin de la liste. Un jeu déjà présent dans la liste est ignoré (pas de doublon). */
    private boolean addGamesInGamesList(long gamesListId, List<GameSearchResult> games) {
        if (games == null || games.isEmpty()) {
            return false;
        }
        GamesList gamesList = gamesListRepository.findById(gamesListId).orElseThrow();
        List<Game> gamesInList = new ArrayList<>(gamesList.getGames().stream().map(GamesListEntry::getGame).toList());
        int nextPosition = gamesList.getGames().stream()
                .mapToInt(GamesListEntry::getPosition)
                .max()
                .orElse(-1);
        boolean gamesAdded = false;
        for (GameSearchResult result : games) {
            Game game = findOrCreateGame(result);
            if (containsGame(gamesInList, game)) {
                continue;
            }
            gamesInList.add(game);
            gamesList.getGames().add(new GamesListEntry(game, ++nextPosition));
            gamesAdded = true;
        }
        if (gamesAdded) {
            gamesListRepository.save(gamesList);
        }
        return gamesAdded;
    }

    /** Renumérote les jeux restants de 0 à n-1, dans leur ordre actuel, pour combler les trous laissés par un retrait. */
    private void manageGamesListEntryPosition(long gamesListId) {
        List<GamesListEntry> gamesListEntryList = gamesListEntryRepository.findByGamesListId(gamesListId).stream()
                .sorted(Comparator.comparingInt(GamesListEntry::getPosition))
                .toList();

        for (int position = 0; position < gamesListEntryList.size(); position++) {
            gamesListEntryList.get(position).setPosition(position);
        }

        gamesListEntryRepository.saveAll(gamesListEntryList);
    }

    @Transactional
    public String deleteGamesList(Long gamesListId, long userId) {
        int gamesDeleted = gamesListRepository.deleteGamesListByIdAndUserId(gamesListId, userId);
        if (gamesDeleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Games list not found");
        }
        return "GamesList deleted";
    }

}
