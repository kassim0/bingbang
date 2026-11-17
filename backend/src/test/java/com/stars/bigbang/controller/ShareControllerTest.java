package com.stars.bigbang.controller;

import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.GamesListDto;
import com.stars.bigbang.dto.response.ShareLinkDto;
import com.stars.bigbang.dto.response.SharedWalletDto;
import com.stars.bigbang.entity.ShareScope;
import com.stars.bigbang.entity.WalletBackground;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.GamesRepository;
import com.stars.bigbang.repository.ShareLinkRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste le partage de bout en bout. « owner » partage, « visitor » est un autre utilisateur qui ouvre le lien.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShareControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GamesListRepository gamesListRepository;
    @Autowired
    private GamesRepository gamesRepository;
    @Autowired
    private ShareLinkRepository shareLinkRepository;

    private String owner;
    private String visitor;
    private GamesListDto loved;
    private GamesListDto wishlist;

    @BeforeEach
    public void setUp() {
        gamesListRepository.deleteAll();
        gamesRepository.deleteAll();
        userRepository.deleteAll(); // la base supprime les liens de partage avec leur compte
        owner = newGuestToken();
        visitor = newGuestToken();
        loved = createList(owner, "Loved", 72);
        wishlist = createList(owner, "Wishlist", 1020);
    }

    // ---------- créer ----------

    @Test
    public void createTest_partage_de_tout_le_wallet() {
        ResponseEntity<ShareLinkDto> response = share(owner, Map.of("scope", "WALLET"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ShareScope.WALLET, response.getBody().scope());
        assertTrue(response.getBody().token().length() >= 40);

        SharedWalletDto shared = view(visitor, response.getBody().token()).getBody();
        assertEquals(List.of("Loved", "Wishlist"), names(shared));
        assertEquals("Game 72", shared.lists().get(0).games().get(0).game().name());
    }

    @Test
    public void createTest_partage_d_une_seule_liste() {
        String token = share(owner, Map.of("scope", "LISTS", "listIds", List.of(wishlist.id()))).getBody().token();

        assertEquals(List.of("Wishlist"), names(view(visitor, token).getBody()));
    }

    @Test
    public void createTest_partage_de_plusieurs_listes_dans_un_seul_lien() {
        createList(owner, "Private", 1942);
        String token = share(owner, Map.of("scope", "LISTS", "listIds", List.of(loved.id(), wishlist.id()))).getBody().token();

        assertEquals(List.of("Loved", "Wishlist"), names(view(visitor, token).getBody()));
    }

    @Test
    public void createTest_chaque_lien_a_un_token_different() {
        String first = share(owner, Map.of("scope", "WALLET")).getBody().token();
        String second = share(owner, Map.of("scope", "WALLET")).getBody().token();

        assertNotEquals(first, second);
    }

    @Test
    public void createTest_cas_invalides() {
        assertEquals(HttpStatus.BAD_REQUEST, share(owner, Map.of()).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, share(owner, Map.of("scope", "EVERYTHING")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, share(owner, Map.of("scope", "LISTS")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, share(owner, Map.of("scope", "LISTS", "listIds", List.of())).getStatusCode());
        assertEquals(0, shareLinkRepository.count());
    }

    @Test
    public void createTest_on_ne_peut_pas_partager_la_liste_d_un_autre() {
        ResponseEntity<ShareLinkDto> response = share(visitor, Map.of("scope", "LISTS", "listIds", List.of(loved.id())));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(0, shareLinkRepository.count());
    }

    @Test
    public void createTest_sans_token_est_refuse() {
        ResponseEntity<String> response = restTemplate.postForEntity("/api/share", Map.of("scope", "WALLET"), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ---------- voir ----------

    @Test
    public void viewTest_wallet_montre_aussi_les_listes_creees_apres_le_partage() {
        String token = share(owner, Map.of("scope", "WALLET")).getBody().token();

        createList(owner, "Created later", 1942);

        assertEquals(List.of("Loved", "Wishlist", "Created later"), names(view(visitor, token).getBody()));
    }

    @Test
    public void viewTest_selection_de_listes_reste_figee() {
        String token = share(owner, Map.of("scope", "LISTS", "listIds", List.of(loved.id()))).getBody().token();

        createList(owner, "Created later", 1942);

        assertEquals(List.of("Loved"), names(view(visitor, token).getBody()));
    }

    @Test
    public void viewTest_commentaires_masques_par_defaut_et_visibles_sur_demande() {
        long gameId = loved.games().get(0).game().id();
        exchange("/api/game/gamesList/" + loved.id() + "/games/" + gameId + "/commentary", HttpMethod.PUT, owner,
                Map.of("text", "My private thoughts"), String.class);

        String withoutComments = share(owner, Map.of("scope", "WALLET")).getBody().token();
        String withComments = share(owner, Map.of("scope", "WALLET", "includeCommentaries", true)).getBody().token();

        assertNull(view(visitor, withoutComments).getBody().lists().get(0).games().get(0).commentary());
        assertEquals("My private thoughts", view(visitor, withComments).getBody().lists().get(0).games().get(0).commentary());
    }

    @Test
    public void viewTest_montre_le_pseudo_et_le_fond_du_proprietaire() {
        exchange("/api/wallet/background", HttpMethod.PUT, owner, Map.of("background", "OCEAN"), String.class);
        String token = share(owner, Map.of("scope", "WALLET")).getBody().token();

        SharedWalletDto shared = view(visitor, token).getBody();

        assertEquals("guest", shared.ownerName());
        assertEquals(WalletBackground.OCEAN, shared.background());
    }

    @Test
    public void viewTest_cas_lien_inconnu() {
        assertEquals(HttpStatus.NOT_FOUND, view(visitor, "not-a-real-token").getStatusCode());
    }

    @Test
    public void viewTest_liste_supprimee_disparait_du_partage() {
        String token = share(owner, Map.of("scope", "LISTS", "listIds", List.of(loved.id(), wishlist.id()))).getBody().token();

        deleteList(owner, loved.id());
        assertEquals(List.of("Wishlist"), names(view(visitor, token).getBody()));

        // plus aucune liste à montrer : le lien ne mène plus à rien
        deleteList(owner, wishlist.id());
        assertEquals(HttpStatus.NOT_FOUND, view(visitor, token).getStatusCode());
    }

    // ---------- lecture seule ----------

    @Test
    public void le_visiteur_ne_peut_ni_modifier_ni_supprimer_la_liste_partagee() {
        String token = share(owner, Map.of("scope", "WALLET")).getBody().token();
        view(visitor, token);

        ResponseEntity<String> update = exchange("/api/game/updateGamesList", HttpMethod.POST, visitor,
                Map.of("gamesListId", loved.id(), "newName", "Hacked"), String.class);
        ResponseEntity<String> delete = exchange("/api/game/deleteGamesList/" + loved.id(), HttpMethod.DELETE, visitor, null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, update.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, delete.getStatusCode());
        assertEquals(List.of("Loved", "Wishlist"), names(view(visitor, token).getBody()));
    }

    // ---------- mes liens / révoquer ----------

    @Test
    public void findMineTest_liste_mes_liens_avec_le_nom_des_listes() {
        share(owner, Map.of("scope", "WALLET"));
        share(owner, Map.of("scope", "LISTS", "listIds", List.of(wishlist.id())));

        ShareLinkDto[] mine = myLinks(owner);

        assertEquals(2, mine.length);
        // le plus récent d'abord
        assertEquals(ShareScope.LISTS, mine[0].scope());
        assertEquals(List.of("Wishlist"), mine[0].listNames());
        assertEquals(ShareScope.WALLET, mine[1].scope());
        assertEquals(0, myLinks(visitor).length);
    }

    @Test
    public void revokeTest_le_lien_ne_mene_plus_a_rien() {
        ShareLinkDto link = share(owner, Map.of("scope", "WALLET")).getBody();

        ResponseEntity<Void> response = exchange("/api/share/" + link.id(), HttpMethod.DELETE, owner, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, view(visitor, link.token()).getStatusCode());
        assertEquals(0, myLinks(owner).length);
    }

    @Test
    public void revokeTest_on_ne_peut_pas_revoquer_le_lien_d_un_autre() {
        ShareLinkDto link = share(owner, Map.of("scope", "WALLET")).getBody();

        ResponseEntity<Void> response = exchange("/api/share/" + link.id(), HttpMethod.DELETE, visitor, null, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.OK, view(visitor, link.token()).getStatusCode());
    }

    // ---------- outils ----------

    private String newGuestToken() {
        return restTemplate.postForEntity("/api/auth/guest", null, AuthResponseDto.class).getBody().token();
    }

    private GamesListDto createList(String token, String name, int igdbId) {
        Map<String, Object> game = Map.of("source", "IGDB", "externalId", igdbId, "slug", "game-" + igdbId, "name", "Game " + igdbId);
        return exchange("/api/game/newGamesList", HttpMethod.PUT, token, Map.of("name", name, "games", List.of(game)), GamesListDto.class).getBody();
    }

    private void deleteList(String token, long listId) {
        assertEquals(HttpStatus.OK, exchange("/api/game/deleteGamesList/" + listId, HttpMethod.DELETE, token, null, String.class).getStatusCode());
    }

    private ResponseEntity<ShareLinkDto> share(String token, Map<String, Object> body) {
        return exchange("/api/share", HttpMethod.POST, token, body, ShareLinkDto.class);
    }

    private ShareLinkDto[] myLinks(String token) {
        return exchange("/api/share", HttpMethod.GET, token, null, ShareLinkDto[].class).getBody();
    }

    private ResponseEntity<SharedWalletDto> view(String token, String shareToken) {
        return exchange("/api/share/view/" + shareToken, HttpMethod.GET, token, null, SharedWalletDto.class);
    }

    private List<String> names(SharedWalletDto shared) {
        return shared.lists().stream().map(GamesListDto::name).toList();
    }

    private <T> ResponseEntity<T> exchange(String url, HttpMethod method, String token, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), responseType);
    }
}
