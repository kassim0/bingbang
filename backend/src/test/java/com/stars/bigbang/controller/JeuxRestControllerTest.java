package com.stars.bigbang.controller;

import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.GamesListDto;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste les routes des listes de bout en bout : vraie configuration de sécurité, vraie base (H2 en mémoire),
 * vraies requêtes SQL. Chaque test se connecte avec un compte invité tout neuf.
 * Les payloads sont écrits sous forme de Map pour pouvoir envoyer aussi des données invalides.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JeuxRestControllerTest {

    private static final String API_NEW = "/api/game/newGamesList";
    private static final String API_GET = "/api/game/getGamesLists";
    private static final String API_UPDATE = "/api/game/updateGamesList";
    private static final String API_DELETE = "/api/game/deleteGamesList/";
    private static final String IGDB_IMAGE = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1rs4.jpg";

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GamesListRepository gamesListRepository;
    @Autowired
    private GamesRepository gamesRepository;

    private String token;

    @BeforeEach
    public void cleanDatabaseAndConnect() {
        // les listes d'abord : elles référencent les comptes et les jeux
        gamesListRepository.deleteAll();
        gamesRepository.deleteAll();
        userRepository.deleteAll();
        token = newGuestToken();
    }

    // ---------- création ----------

    @Test
    public void createTest_cas_nominal() {
        ResponseEntity<GamesListDto> response = create(token, "Mes jeux", game(72, "Portal 2"), game(1020, "GTA V"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GamesListDto list = response.getBody();
        assertEquals("Mes jeux", list.name());
        assertEquals(List.of("Portal 2", "GTA V"), gameNames(list));
        assertEquals(List.of(0, 1), list.games().stream().map(entry -> entry.position()).toList());
        assertEquals(IGDB_IMAGE, list.games().get(0).game().backgroundImage());
    }

    @Test
    public void createTest_sans_token_est_refuse() {
        ResponseEntity<Map> response = restTemplate.exchange(API_NEW, HttpMethod.PUT,
                new HttpEntity<>(Map.of("name", "Mes jeux", "games", List.of())), Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(0, gamesListRepository.count());
    }

    @Test
    public void createTest_champ_games_absent_cree_une_liste_vide() {
        ResponseEntity<GamesListDto> response = exchange(API_NEW, HttpMethod.PUT, token, Map.of("name", "Vide"), GamesListDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().games().isEmpty());
    }

    @Test
    public void createTest_meme_jeu_deux_fois_n_est_ajoute_qu_une_fois() {
        ResponseEntity<GamesListDto> response = create(token, "Doublon", game(72, "Portal 2"), game(72, "Portal 2"));

        assertEquals(List.of("Portal 2"), gameNames(response.getBody()));
    }

    @Test
    public void createTest_les_espaces_autour_du_nom_sont_retires() {
        assertEquals("Mes jeux", create(token, "  Mes jeux  ").getBody().name());
    }

    @Test
    public void createTest_cas_nom_trop_long() {
        assertBadRequest(API_NEW, HttpMethod.PUT, Map.of("name", "x".repeat(101), "games", List.of()));
        assertEquals(0, gamesListRepository.count());
    }

    @Test
    public void createTest_cas_jeu_sans_nom() {
        Map<String, Object> game = game(72, "Portal 2");
        game.remove("name");

        assertBadRequest(API_NEW, HttpMethod.PUT, Map.of("name", "Mes jeux", "games", List.of(game)));
        assertEquals(0, gamesRepository.count());
    }

    @Test
    public void createTest_cas_jeu_sans_source_ni_identifiant() {
        assertBadRequest(API_NEW, HttpMethod.PUT, Map.of("games", List.of(Map.of("name", "Portal 2"))));
        assertEquals(0, gamesRepository.count());
    }

    @Test
    public void createTest_cas_source_inconnue() {
        Map<String, Object> game = game(72, "Portal 2");
        game.put("source", "STEAM");

        assertBadRequest(API_NEW, HttpMethod.PUT, Map.of("games", List.of(game)));
    }

    @Test
    public void createTest_image_d_un_serveur_inconnu_n_est_pas_enregistree() {
        Map<String, Object> game = game(72, "Portal 2");
        game.put("imageUrl", "https://evil.example/track.png");

        GamesListDto list = create(token, "Mes jeux", game).getBody();

        assertNull(list.games().get(0).game().backgroundImage());
    }

    // ---------- lecture ----------

    @Test
    public void getTest_listes_dans_l_ordre_de_creation_avec_leurs_jeux() {
        create(token, "Première", game(72, "Portal 2"));
        create(token, "Deuxième");

        GamesListDto[] lists = getLists(token);

        assertEquals(2, lists.length);
        assertEquals("Première", lists[0].name());
        assertEquals(List.of("Portal 2"), gameNames(lists[0]));
        assertEquals("Deuxième", lists[1].name());
    }

    @Test
    public void getTest_un_utilisateur_ne_voit_pas_les_listes_d_un_autre() {
        create(token, "À moi");

        assertEquals(0, getLists(newGuestToken()).length);
    }

    // ---------- modification ----------

    @Test
    public void updateTest_renomme_ajoute_et_retire_des_jeux() {
        GamesListDto list = create(token, "Avant", game(72, "Portal 2"), game(1020, "GTA V")).getBody();
        long portalId = list.games().get(0).game().id();

        ResponseEntity<String> response = update(token, Map.of(
                "gamesListId", list.id(),
                "newName", "Après",
                "removeGameId", List.of(portalId),
                "newGames", List.of(game(1942, "The Witcher 3"))));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GamesListDto updated = getLists(token)[0];
        assertEquals("Après", updated.name());
        assertEquals(List.of("GTA V", "The Witcher 3"), gameNames(updated));
        assertEquals(List.of(0, 1), updated.games().stream().map(entry -> entry.position()).toList());
    }

    @Test
    public void updateTest_jeu_deja_dans_la_liste_n_est_pas_ajoute_en_double() {
        GamesListDto list = create(token, "Liste", game(72, "Portal 2")).getBody();

        update(token, Map.of("gamesListId", list.id(), "newGames", List.of(game(72, "Portal 2"))));

        assertEquals(List.of("Portal 2"), gameNames(getLists(token)[0]));
    }

    @Test
    public void updateTest_cas_nouveau_nom_vide() {
        GamesListDto list = create(token, "Liste").getBody();

        assertBadRequest(API_UPDATE, HttpMethod.POST, Map.of("gamesListId", list.id(), "newName", "   "));
        assertEquals("Liste", getLists(token)[0].name());
    }

    @Test
    public void updateTest_cas_liste_d_un_autre_utilisateur() {
        GamesListDto list = create(token, "À moi").getBody();

        ResponseEntity<String> response = update(newGuestToken(), Map.of("gamesListId", list.id(), "newName", "Volée"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("À moi", getLists(token)[0].name());
    }

    // ---------- suppression ----------

    @Test
    public void deleteTest_cas_nominal() {
        GamesListDto list = create(token, "À supprimer", game(72, "Portal 2")).getBody();

        ResponseEntity<String> response = exchange(API_DELETE + list.id(), HttpMethod.DELETE, token, null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, getLists(token).length);
    }

    @Test
    public void deleteTest_cas_liste_d_un_autre_utilisateur() {
        GamesListDto list = create(token, "À moi").getBody();

        ResponseEntity<String> response = exchange(API_DELETE + list.id(), HttpMethod.DELETE, newGuestToken(), null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(1, getLists(token).length);
    }

    // ---------- outils ----------

    private String newGuestToken() {
        AuthResponseDto guest = restTemplate.postForEntity("/api/auth/guest", null, AuthResponseDto.class).getBody();
        assertNotNull(guest);
        return guest.token();
    }

    /** Un jeu tel que le navigateur le renvoie après une recherche. HashMap pour pouvoir le modifier dans un test. */
    private Map<String, Object> game(int igdbId, String name) {
        Map<String, Object> game = new HashMap<>();
        game.put("source", "IGDB");
        game.put("externalId", igdbId);
        game.put("slug", name.toLowerCase().replace(' ', '-'));
        game.put("name", name);
        game.put("releaseDate", "2011-04-19");
        game.put("imageUrl", IGDB_IMAGE);
        return game;
    }

    @SafeVarargs
    private ResponseEntity<GamesListDto> create(String token, String name, Map<String, Object>... games) {
        return exchange(API_NEW, HttpMethod.PUT, token, Map.of("name", name, "games", List.of(games)), GamesListDto.class);
    }

    private ResponseEntity<String> update(String token, Map<String, Object> body) {
        return exchange(API_UPDATE, HttpMethod.POST, token, body, String.class);
    }

    private GamesListDto[] getLists(String token) {
        return exchange(API_GET, HttpMethod.GET, token, null, GamesListDto[].class).getBody();
    }

    private List<String> gameNames(GamesListDto list) {
        return list.games().stream().map(entry -> entry.game().name()).toList();
    }

    private void assertBadRequest(String url, HttpMethod method, Map<String, Object> body) {
        assertEquals(HttpStatus.BAD_REQUEST, exchange(url, method, token, body, Map.class).getStatusCode());
    }

    private <T> ResponseEntity<T> exchange(String url, HttpMethod method, String token, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), responseType);
    }
}
