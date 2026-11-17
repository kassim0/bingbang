package com.stars.bigbang.controller;

import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.CommentaryDto;
import com.stars.bigbang.dto.response.GamesListDto;
import com.stars.bigbang.repository.GameCommentaryRepository;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.GamesRepository;
import com.stars.bigbang.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Teste les commentaires de bout en bout (sécurité, base H2, SQL), règle par règle du wiki.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentaryControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GamesListRepository gamesListRepository;
    @Autowired
    private GamesRepository gamesRepository;
    @Autowired
    private GameCommentaryRepository commentaryRepository;

    private String token;
    private long listId;
    private long portalId;

    @BeforeEach
    public void setUp() {
        gamesListRepository.deleteAll(); // la base supprime les commentaires avec leur liste
        gamesRepository.deleteAll();
        userRepository.deleteAll();
        token = newGuestToken();
        GamesListDto list = createList(token, "Loved", 72);
        listId = list.id();
        portalId = list.games().get(0).game().id();
    }

    // ---------- ajouter / modifier ----------

    @Test
    public void saveTest_cas_nominal() {
        ResponseEntity<CommentaryDto> response = save(token, listId, portalId, "  Best puzzle game ever  ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Best puzzle game ever", response.getBody().text());
        assertEquals("Best puzzle game ever", commentaryOf(token, listId, portalId));
    }

    @Test
    public void saveTest_un_second_envoi_remplace_le_texte() {
        save(token, listId, portalId, "First");
        save(token, listId, portalId, "Second");

        assertEquals("Second", commentaryOf(token, listId, portalId));
        assertEquals(1, commentaryRepository.count());
    }

    @Test
    public void saveTest_cas_texte_vide_ou_trop_long() {
        assertEquals(HttpStatus.BAD_REQUEST, save(token, listId, portalId, "   ").getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, save(token, listId, portalId, "x".repeat(2001)).getStatusCode());
        assertEquals(0, commentaryRepository.count());
    }

    @Test
    public void saveTest_cas_jeu_absent_de_la_liste() {
        long otherGameId = createList(token, "Other", 1020).games().get(0).game().id();

        assertEquals(HttpStatus.NOT_FOUND, save(token, listId, otherGameId, "Hello").getStatusCode());
    }

    @Test
    public void saveTest_cas_liste_d_un_autre_utilisateur() {
        assertEquals(HttpStatus.NOT_FOUND, save(newGuestToken(), listId, portalId, "Hacked").getStatusCode());
        assertNull(commentaryOf(token, listId, portalId));
    }

    @Test
    public void saveTest_sans_token_est_refuse() {
        ResponseEntity<String> response = restTemplate.exchange(url(listId, portalId), HttpMethod.PUT,
                new HttpEntity<>(Map.of("text", "Hello")), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ---------- un commentaire par jeu ET par liste ----------

    @Test
    public void meme_jeu_dans_deux_listes_a_deux_commentaires_differents() {
        long otherListId = createList(token, "To replay", 72).id();

        save(token, listId, portalId, "Loved it");
        save(token, otherListId, portalId, "Replay in coop");

        assertEquals("Loved it", commentaryOf(token, listId, portalId));
        assertEquals("Replay in coop", commentaryOf(token, otherListId, portalId));
    }

    // ---------- le commentaire survit au retrait du jeu ----------

    @Test
    public void commentaire_conserve_quand_le_jeu_est_retire_puis_rajoute() {
        save(token, listId, portalId, "Loved it");

        update(Map.of("gamesListId", listId, "removeGameId", List.of(portalId)));
        assertEquals(0, lists(token)[0].games().size());
        assertEquals(1, commentaryRepository.count());

        update(Map.of("gamesListId", listId, "newGames", List.of(game(72))));
        assertEquals("Loved it", commentaryOf(token, listId, portalId));
    }

    // ---------- supprimer ----------

    @Test
    public void deleteTest_cas_nominal() {
        save(token, listId, portalId, "Loved it");

        ResponseEntity<Void> response = exchange(url(listId, portalId), HttpMethod.DELETE, token, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(commentaryOf(token, listId, portalId));
    }

    @Test
    public void deleteTest_cas_liste_d_un_autre_utilisateur() {
        save(token, listId, portalId, "Loved it");

        ResponseEntity<Void> response = exchange(url(listId, portalId), HttpMethod.DELETE, newGuestToken(), null, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Loved it", commentaryOf(token, listId, portalId));
    }

    @Test
    public void supprimer_la_liste_supprime_ses_commentaires() {
        save(token, listId, portalId, "Loved it");

        exchange("/api/game/deleteGamesList/" + listId, HttpMethod.DELETE, token, null, String.class);

        assertEquals(0, commentaryRepository.count());
    }

    // ---------- transférer (copier) vers une autre liste ----------

    @Test
    public void transferTest_copie_et_garde_l_original() {
        long targetListId = createList(token, "Target", 72).id();
        save(token, listId, portalId, "Loved it");

        ResponseEntity<CommentaryDto> response = transfer(token, listId, portalId, Map.of("targetListId", targetListId));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Loved it", commentaryOf(token, targetListId, portalId));
        assertEquals("Loved it", commentaryOf(token, listId, portalId));
    }

    @Test
    public void transferTest_cas_cible_deja_commentee() {
        long targetListId = createList(token, "Target", 72).id();
        save(token, listId, portalId, "From source");
        save(token, targetListId, portalId, "Already there");

        ResponseEntity<CommentaryDto> refused = transfer(token, listId, portalId, Map.of("targetListId", targetListId));
        assertEquals(HttpStatus.CONFLICT, refused.getStatusCode());
        assertEquals("Already there", commentaryOf(token, targetListId, portalId));

        ResponseEntity<CommentaryDto> replaced = transfer(token, listId, portalId, Map.of("targetListId", targetListId, "replace", true));
        assertEquals(HttpStatus.OK, replaced.getStatusCode());
        assertEquals("From source", commentaryOf(token, targetListId, portalId));
    }

    @Test
    public void transferTest_la_cible_n_a_pas_besoin_de_contenir_le_jeu() {
        long targetListId = createList(token, "Empty target").id();
        save(token, listId, portalId, "Loved it");

        assertEquals(HttpStatus.OK, transfer(token, listId, portalId, Map.of("targetListId", targetListId)).getStatusCode());

        // le commentaire attend le jeu : il apparaît dès que le jeu est ajouté à la liste cible
        update(Map.of("gamesListId", targetListId, "newGames", List.of(game(72))));
        assertEquals("Loved it", commentaryOf(token, targetListId, portalId));
    }

    @Test
    public void transferTest_cas_erreurs() {
        long targetListId = createList(token, "Target").id();
        long foreignListId = createList(newGuestToken(), "Not mine").id();

        // pas de commentaire à copier
        assertEquals(HttpStatus.NOT_FOUND, transfer(token, listId, portalId, Map.of("targetListId", targetListId)).getStatusCode());
        save(token, listId, portalId, "Loved it");
        // même liste
        assertEquals(HttpStatus.BAD_REQUEST, transfer(token, listId, portalId, Map.of("targetListId", listId)).getStatusCode());
        // cible manquante
        assertEquals(HttpStatus.BAD_REQUEST, transfer(token, listId, portalId, Map.of()).getStatusCode());
        // liste cible d'un autre utilisateur
        assertEquals(HttpStatus.NOT_FOUND, transfer(token, listId, portalId, Map.of("targetListId", foreignListId)).getStatusCode());
        assertEquals(1, commentaryRepository.count());
    }

    // ---------- outils ----------

    private String newGuestToken() {
        return restTemplate.postForEntity("/api/auth/guest", null, AuthResponseDto.class).getBody().token();
    }

    private Map<String, Object> game(int igdbId) {
        return Map.of("source", "IGDB", "externalId", igdbId, "slug", "game-" + igdbId, "name", "Game " + igdbId);
    }

    private GamesListDto createList(String token, String name, int... igdbIds) {
        List<Map<String, Object>> games = java.util.Arrays.stream(igdbIds).mapToObj(this::game).toList();
        return exchange("/api/game/newGamesList", HttpMethod.PUT, token, Map.of("name", name, "games", games), GamesListDto.class).getBody();
    }

    private void update(Map<String, Object> body) {
        assertEquals(HttpStatus.OK, exchange("/api/game/updateGamesList", HttpMethod.POST, token, body, String.class).getStatusCode());
    }

    private GamesListDto[] lists(String token) {
        return exchange("/api/game/getGamesLists", HttpMethod.GET, token, null, GamesListDto[].class).getBody();
    }

    /** Commentaire affiché pour ce jeu dans cette liste, tel que la home le reçoit ; null s'il n'y en a pas ou si le jeu n'y est pas. */
    private String commentaryOf(String token, long listId, long gameId) {
        for (GamesListDto list : lists(token)) {
            if (list.id() == listId) {
                // boucle simple : Optional.map() refuserait un commentaire null
                for (var entry : list.games()) {
                    if (entry.game().id() == gameId) {
                        return entry.commentary();
                    }
                }
            }
        }
        return null;
    }

    private String url(long listId, long gameId) {
        return "/api/game/gamesList/" + listId + "/games/" + gameId + "/commentary";
    }

    private ResponseEntity<CommentaryDto> save(String token, long listId, long gameId, String text) {
        return exchange(url(listId, gameId), HttpMethod.PUT, token, Map.of("text", text), CommentaryDto.class);
    }

    private ResponseEntity<CommentaryDto> transfer(String token, long listId, long gameId, Map<String, Object> body) {
        return exchange(url(listId, gameId) + "/transfer", HttpMethod.POST, token, body, CommentaryDto.class);
    }

    private <T> ResponseEntity<T> exchange(String url, HttpMethod method, String token, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), responseType);
    }
}
