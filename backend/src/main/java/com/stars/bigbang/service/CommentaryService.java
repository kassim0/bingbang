package com.stars.bigbang.service;

import com.stars.bigbang.dto.response.CommentaryDto;
import com.stars.bigbang.dto.response.GamesListDto;
import com.stars.bigbang.dto.response.GamesListEntryDto;
import com.stars.bigbang.entity.GameCommentary;
import com.stars.bigbang.repository.GameCommentaryRepository;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.GamesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Commentaires des jeux d'une liste. Règles (wiki, page Features) :
 * - un commentaire appartient à un jeu DANS une liste : le même jeu dans deux listes a deux commentaires ;
 * - il survit au retrait du jeu, et réapparaît si le jeu est rajouté ;
 * - il ne disparaît que si on le supprime explicitement (ou si on supprime la liste).
 */
@Service
@RequiredArgsConstructor
public class CommentaryService {

    private final GameCommentaryRepository commentaryRepository;
    private final GamesListRepository gamesListRepository;
    private final GamesRepository gamesRepository;

    /** Crée le commentaire, ou remplace son texte s'il existe déjà. Le jeu doit être dans la liste. */
    @Transactional
    public CommentaryDto save(long listId, long gameId, String text, long userId) {
        assertOwnList(listId, userId);
        if (!gamesListRepository.existsGameInList(listId, gameId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found in this list");
        }
        return CommentaryDto.from(upsert(listId, gameId, text.strip()));
    }

    /** Supprime le commentaire. Ne fait rien s'il n'y en avait pas : le résultat demandé est déjà atteint. */
    @Transactional
    public void delete(long listId, long gameId, long userId) {
        assertOwnList(listId, userId);
        commentaryRepository.findByListAndGame(listId, gameId).ifPresent(commentaryRepository::delete);
    }

    /**
     * Copie le commentaire d'une liste vers une autre liste du même utilisateur.
     * Le commentaire d'origine est conservé. La liste cible n'a pas besoin de contenir le jeu :
     * le commentaire y attendra que le jeu soit ajouté.
     *
     * @param replace false : refuse (409) si la liste cible a déjà un commentaire sur ce jeu
     */
    @Transactional
    public CommentaryDto copyToList(long sourceListId, long gameId, long targetListId, boolean replace, long userId) {
        assertOwnList(sourceListId, userId);
        assertOwnList(targetListId, userId);
        if (sourceListId == targetListId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and target lists are the same");
        }
        GameCommentary source = commentaryRepository.findByListAndGame(sourceListId, gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentary not found"));
        if (!replace && commentaryRepository.findByListAndGame(targetListId, gameId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The target list already has a commentary on this game");
        }
        return CommentaryDto.from(upsert(targetListId, gameId, source.getText()));
    }

    /** Ajoute à chaque jeu de chaque liste son commentaire, en une seule requête pour toutes les listes. */
    @Transactional(readOnly = true)
    public List<GamesListDto> withCommentaries(List<GamesListDto> lists) {
        if (lists.isEmpty()) {
            return lists;
        }
        // clé : "idListe:idJeu"
        Map<String, String> texts = new HashMap<>();
        for (GameCommentary commentary : commentaryRepository.findAllByListIds(lists.stream().map(GamesListDto::id).toList())) {
            texts.put(commentary.getGamesList().getId() + ":" + commentary.getGame().getId(), commentary.getText());
        }
        return lists.stream().map(list -> {
            List<GamesListEntryDto> games = list.games().stream()
                    .map(entry -> entry.withCommentary(texts.get(list.id() + ":" + entry.game().id())))
                    .toList();
            return new GamesListDto(list.id(), list.name(), list.position(), games);
        }).toList();
    }

    private GameCommentary upsert(long listId, long gameId, String text) {
        GameCommentary commentary = commentaryRepository.findByListAndGame(listId, gameId)
                .orElseGet(() -> new GameCommentary(gamesListRepository.getReferenceById(listId), gamesRepository.getReferenceById(gameId), text));
        commentary.setText(text);
        return commentaryRepository.save(commentary);
    }

    /** 404 et non 403 : on ne confirme pas l'existence de la liste d'un autre (décision du wiki). */
    private void assertOwnList(long listId, long userId) {
        if (!gamesListRepository.existsByIdAndUserId(listId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Games list not found");
        }
    }
}
