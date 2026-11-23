package com.stars.bigbang.controller;

import com.stars.bigbang.dto.payload.LoginRequestDto;
import com.stars.bigbang.dto.payload.RegisterRequestDto;
import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.GamesListDto;
import com.stars.bigbang.dto.response.UserDto;
import com.stars.bigbang.entity.GamesList;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.UserRepository;
import com.stars.bigbang.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GamesListRepository gamesListRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String API_REGISTER = "/api/auth/register";
    private static final String API_LOGIN = "/api/auth/login";
    private static final String API_ME = "/api/auth/me";
    private static final String API_GUEST = "/api/auth/guest";
    private static final String API_GET_GAMES_LISTS = "/api/game/getGamesLists";

    /** guest-<uuid>@bingbang.invalid : .invalid est un domaine réservé, aucun mail ne peut y partir. */
    private static final String GUEST_EMAIL = "^guest-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}@bingbang\\.invalid$";

    private static final String USERNAME = "test";
    private static final String EMAIL = "test@test.com";
    private static final String PASSWORD = "password123";

    @BeforeEach
    public void cleanDatabase() {
        // les listes d'abord : elles référencent les comptes (clé étrangère user_id)
        gamesListRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------- POST /api/auth/register ----------

    @Test
    public void registerTest_cas_nominal() {
        ResponseEntity<AuthResponseDto> response = register(USERNAME, EMAIL, PASSWORD);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AuthResponseDto body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.token());
        assertFalse(body.token().isBlank());
        assertEquals(EMAIL, body.user().email());
        assertEquals(USERNAME, body.user().username());
        assertFalse(body.user().guest());

        User newUser = userRepository.findByEmail(EMAIL).orElseThrow();
        assertEquals(USERNAME, newUser.getUsername());
        assertFalse(newUser.isGuest());
        assertNotEquals(PASSWORD, newUser.getPassword());
        assertTrue(passwordEncoder.matches(PASSWORD, newUser.getPassword()));
    }

    @Test
    public void registerTest_email_normalise_en_minuscules() {
        ResponseEntity<AuthResponseDto> response = register(USERNAME, "TeSt@TeSt.CoM", PASSWORD);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(EMAIL, response.getBody().user().email());
        assertTrue(userRepository.findByEmail(EMAIL).isPresent());
    }

    @Test
    public void registerTest_token_utilisable_sur_me() {
        String token = register(USERNAME, EMAIL, PASSWORD).getBody().token();

        ResponseEntity<UserDto> response = me("Bearer " + token, UserDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(EMAIL, response.getBody().email());
    }

    @Test
    public void registerTest_cas_email_existant() {
        register("test1", EMAIL, PASSWORD);

        ResponseEntity<Map> response = restTemplate.postForEntity(API_REGISTER,
                new RegisterRequestDto("test2", EMAIL, PASSWORD), Map.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already in use", response.getBody().get("message"));
        assertEquals(1, userRepository.count());
    }

    @Test
    public void registerTest_cas_email_existant_casse_differente() {
        register("test1", EMAIL, PASSWORD);

        ResponseEntity<Map> response = restTemplate.postForEntity(API_REGISTER,
                new RegisterRequestDto("test2", "TEST@TEST.COM", PASSWORD), Map.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void registerTest_cas_mot_de_passe_trop_court() {
        assertRegisterBadRequest(USERNAME, EMAIL, "1234567");
    }

    @Test
    public void registerTest_cas_mot_de_passe_trop_long() {
        assertRegisterBadRequest(USERNAME, EMAIL, "a".repeat(73));
    }

    @Test
    public void registerTest_cas_email_invalide() {
        assertRegisterBadRequest(USERNAME, "pas-un-email", PASSWORD);
    }

    @Test
    public void registerTest_cas_champs_vides() {
        assertRegisterBadRequest("", EMAIL, PASSWORD);
        assertRegisterBadRequest(USERNAME, "", PASSWORD);
        assertRegisterBadRequest(USERNAME, EMAIL, "");
    }

    // ---------- POST /api/auth/login ----------

    @Test
    public void loginTest_cas_nominal() {
        register(USERNAME, EMAIL, PASSWORD);

        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(API_LOGIN,
                new LoginRequestDto(EMAIL, PASSWORD), AuthResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponseDto body = response.getBody();
        assertNotNull(body.token());
        assertEquals(EMAIL, body.user().email());
        assertEquals(USERNAME, body.user().username());
        assertEquals(HttpStatus.OK, me("Bearer " + body.token(), UserDto.class).getStatusCode());
    }

    @Test
    public void loginTest_email_insensible_a_la_casse() {
        register(USERNAME, EMAIL, PASSWORD);

        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(API_LOGIN,
                new LoginRequestDto("TEST@test.COM", PASSWORD), AuthResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void loginTest_cas_mauvais_mot_de_passe() {
        register(USERNAME, EMAIL, PASSWORD);

        ResponseEntity<Map> response = restTemplate.postForEntity(API_LOGIN,
                new LoginRequestDto(EMAIL, "mauvaisMotDePasse"), Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("wrong auth", response.getBody().get("message"));
    }

    @Test
    public void loginTest_cas_email_inconnu() {
        ResponseEntity<Map> response = restTemplate.postForEntity(API_LOGIN,
                new LoginRequestDto("inconnu@test.com", PASSWORD), Map.class);

        // même réponse que pour un mauvais mot de passe : on ne révèle pas si l'email existe
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("wrong auth", response.getBody().get("message"));
    }

    @Test
    public void loginTest_cas_champs_vides() {
        ResponseEntity<Map> response = restTemplate.postForEntity(API_LOGIN,
                new LoginRequestDto("", ""), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ---------- GET /api/auth/me ----------

    @Test
    public void meTest_cas_nominal() {
        String token = register(USERNAME, EMAIL, PASSWORD).getBody().token();

        ResponseEntity<UserDto> response = me("Bearer " + token, UserDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(EMAIL, response.getBody().email());
        assertEquals(USERNAME, response.getBody().username());
        assertFalse(response.getBody().guest());
    }

    @Test
    public void meTest_cas_sans_token() {
        ResponseEntity<Map> response = me(null, Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        // le corps JSON est produit par /error grâce au sendError de l'entry point
        assertEquals(401, response.getBody().get("status"));
        assertEquals(API_ME, response.getBody().get("path"));
    }

    @Test
    public void meTest_cas_token_invalide() {
        assertEquals(HttpStatus.UNAUTHORIZED, me("Bearer abc", Map.class).getStatusCode());
    }

    @Test
    public void meTest_cas_token_vide() {
        assertEquals(HttpStatus.UNAUTHORIZED, me("Bearer ", Map.class).getStatusCode());
    }

    @Test
    public void meTest_cas_sans_prefixe_bearer() {
        String token = register(USERNAME, EMAIL, PASSWORD).getBody().token();

        assertEquals(HttpStatus.UNAUTHORIZED, me(token, Map.class).getStatusCode());
    }

    @Test
    public void meTest_cas_token_expire() {
        register(USERNAME, EMAIL, PASSWORD);
        String expiredToken = new JwtService(jwtSecret, -1000,0).generateToken(EMAIL);

        assertEquals(HttpStatus.UNAUTHORIZED, me("Bearer " + expiredToken, Map.class).getStatusCode());
    }

    @Test
    public void meTest_cas_token_signe_avec_une_autre_cle() {
        register(USERNAME, EMAIL, PASSWORD);
        String forgedToken = new JwtService("une-autre-cle-secrete-qui-n-est-pas-celle-du-serveur-123456", 86400000,0)
                .generateToken(EMAIL);

        assertEquals(HttpStatus.UNAUTHORIZED, me("Bearer " + forgedToken, Map.class).getStatusCode());
    }

    @Test
    public void meTest_cas_utilisateur_supprime() {
        String token = register(USERNAME, EMAIL, PASSWORD).getBody().token();
        userRepository.deleteAll();

        assertEquals(HttpStatus.UNAUTHORIZED, me("Bearer " + token, Map.class).getStatusCode());
    }

    @Test
    public void meTest_cas_token_valide_pour_un_email_inconnu() {
        String token = jwtService.generateToken("fantome@test.com");

        assertEquals(HttpStatus.UNAUTHORIZED, me("Bearer " + token, Map.class).getStatusCode());
    }

    // ---------- POST /api/auth/guest ----------

    @Test
    public void guestTest_cas_nominal() {
        ResponseEntity<AuthResponseDto> response = guest();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponseDto body = response.getBody();
        assertNotNull(body);
        assertFalse(body.token().isBlank());
        assertTrue(body.user().guest());
        assertTrue(body.user().email().matches(GUEST_EMAIL), "email invité inattendu : " + body.user().email());

        User guest = userRepository.findByEmail(body.user().email()).orElseThrow();
        assertTrue(guest.isGuest());
        assertFalse(guest.getPassword().isBlank());
    }

    @Test
    public void guestTest_chaque_appel_cree_un_compte_different() {
        String firstEmail = guest().getBody().user().email();
        String secondEmail = guest().getBody().user().email();

        assertNotEquals(firstEmail, secondEmail);
        assertEquals(2, userRepository.count());
    }

    @Test
    public void guestTest_le_token_permet_d_appeler_me() {
        AuthResponseDto body = guest().getBody();

        ResponseEntity<UserDto> response = me("Bearer " + body.token(), UserDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(body.user().email(), response.getBody().email());
        assertTrue(response.getBody().guest(), "/me doit indiquer que c'est un invité, sinon le front le prend pour un vrai compte");
    }

    @Test
    public void guestTest_en_get_est_refuse() {
        ResponseEntity<Map> response = restTemplate.getForEntity(API_GUEST, Map.class);

        assertNotEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, userRepository.count());
    }

    // ---------- POST /api/auth/register par un invité ----------

    @Test
    public void registerTest_invite_devient_un_vrai_compte_et_garde_ses_listes() {
        AuthResponseDto guest = guest().getBody();
        User guestUser = userRepository.findByEmail(guest.user().email()).orElseThrow();
        createGamesList(guestUser, "Liste de l'invité");

        ResponseEntity<AuthResponseDto> response = register(USERNAME, "TeSt@TeSt.CoM", PASSWORD, guest.token());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AuthResponseDto body = response.getBody();
        assertEquals(EMAIL, body.user().email());
        assertEquals(USERNAME, body.user().username());
        assertFalse(body.user().guest());

        // même compte transformé, pas un nouveau compte à côté
        assertEquals(1, userRepository.count());
        User user = userRepository.findById(guestUser.getId()).orElseThrow();
        assertEquals(EMAIL, user.getEmail());
        assertFalse(user.isGuest());
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPassword()));

        // le nouveau token retrouve la liste créée en tant qu'invité
        ResponseEntity<GamesListDto[]> lists = getGamesLists(body.token());
        assertEquals(HttpStatus.OK, lists.getStatusCode());
        assertEquals(1, lists.getBody().length);
        assertEquals("Liste de l'invité", lists.getBody()[0].name());

        // l'ancien token invité portait l'ancien email : il ne doit plus fonctionner
        assertEquals(HttpStatus.UNAUTHORIZED, me("Bearer " + guest.token(), Map.class).getStatusCode());
        // et on peut se reconnecter normalement avec les nouveaux identifiants
        assertEquals(HttpStatus.OK, restTemplate.postForEntity(API_LOGIN,
                new LoginRequestDto(EMAIL, PASSWORD), AuthResponseDto.class).getStatusCode());
    }

    @Test
    public void registerTest_invite_cas_email_existant() {
        register("test1", EMAIL, PASSWORD);
        AuthResponseDto guest = guest().getBody();

        ResponseEntity<Map> response = restTemplate.postForEntity(API_REGISTER,
                withToken(new RegisterRequestDto("test2", "TEST@test.com", PASSWORD), guest.token()), Map.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already in use", response.getBody().get("message"));
        // l'invité est resté invité, avec son token toujours valide
        assertTrue(userRepository.findByEmail(guest.user().email()).orElseThrow().isGuest());
        assertEquals(HttpStatus.OK, me("Bearer " + guest.token(), UserDto.class).getStatusCode());
    }

    @Test
    public void registerTest_cas_vrai_compte_deja_connecte() {
        String token = register(USERNAME, EMAIL, PASSWORD).getBody().token();

        ResponseEntity<Map> response = restTemplate.postForEntity(API_REGISTER,
                withToken(new RegisterRequestDto("autre", "autre@test.com", PASSWORD), token), Map.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("already connected", response.getBody().get("message"));
        assertEquals(1, userRepository.count());
        assertEquals(EMAIL, userRepository.findAll().get(0).getEmail());
    }

    // ---------- POST /api/auth/login par un invité (phase 0b) ----------

    @Test
    public void loginTest_invite_transfere_ses_listes_a_la_suite_de_celles_du_compte() {
        register(USERNAME, EMAIL, PASSWORD);
        User account = userRepository.findByEmail(EMAIL).orElseThrow();
        createGamesList(account, "Liste du compte", 1);
        AuthResponseDto guest = guest().getBody();
        User guestUser = userRepository.findByEmail(guest.user().email()).orElseThrow();
        createGamesList(guestUser, "Invité 1", 1);
        createGamesList(guestUser, "Invité 2", 2);

        ResponseEntity<AuthResponseDto> response = login(EMAIL, PASSWORD, guest.token());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(EMAIL, response.getBody().user().email());

        // les 3 listes appartiennent au compte : la sienne d'abord, puis celles de l'invité, sans trou ni doublon
        GamesListDto[] lists = getGamesLists(response.getBody().token()).getBody();
        assertEquals(3, lists.length);
        assertEquals("Liste du compte", lists[0].name());
        assertEquals("Invité 1", lists[1].name());
        assertEquals("Invité 2", lists[2].name());
        assertEquals(1, lists[0].position());
        assertEquals(2, lists[1].position());
        assertEquals(3, lists[2].position());

        // l'invité n'existe plus, et son ancien token ne fonctionne plus
        assertFalse(userRepository.existsById(guestUser.getId()));
        assertEquals(1, userRepository.count());
        assertEquals(HttpStatus.UNAUTHORIZED, me("Bearer " + guest.token(), Map.class).getStatusCode());
    }

    @Test
    public void loginTest_invite_positions_avec_trou_ordre_conserve() {
        register(USERNAME, EMAIL, PASSWORD);
        User account = userRepository.findByEmail(EMAIL).orElseThrow();
        createGamesList(account, "Compte 1", 1);
        createGamesList(account, "Compte 2", 2);
        AuthResponseDto guest = guest().getBody();
        User guestUser = userRepository.findByEmail(guest.user().email()).orElseThrow();
        // l'invité a supprimé sa liste en position 2 : il reste un trou
        createGamesList(guestUser, "Invité 3", 3);
        createGamesList(guestUser, "Invité 1", 1);

        ResponseEntity<AuthResponseDto> response = login(EMAIL, PASSWORD, guest.token());

        // les positions sont décalées (pas renumérotées) : 1 -> 3, 3 -> 5 ; l'ordre de l'invité est conservé
        GamesListDto[] lists = getGamesLists(response.getBody().token()).getBody();
        assertEquals(4, lists.length);
        assertEquals("Compte 1", lists[0].name());
        assertEquals("Compte 2", lists[1].name());
        assertEquals("Invité 1", lists[2].name());
        assertEquals("Invité 3", lists[3].name());
        assertEquals(3, lists[2].position());
        assertEquals(5, lists[3].position());
    }

    @Test
    public void loginTest_invite_sans_liste_est_supprime() {
        register(USERNAME, EMAIL, PASSWORD);
        AuthResponseDto guest = guest().getBody();

        ResponseEntity<AuthResponseDto> response = login(EMAIL, PASSWORD, guest.token());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(userRepository.findByEmail(guest.user().email()).isEmpty());
        assertEquals(0, getGamesLists(response.getBody().token()).getBody().length);
    }

    @Test
    public void loginTest_invite_cas_mauvais_mot_de_passe_ne_change_rien() {
        register(USERNAME, EMAIL, PASSWORD);
        User account = userRepository.findByEmail(EMAIL).orElseThrow();
        createGamesList(account, "Liste du compte", 1);
        AuthResponseDto guest = guest().getBody();
        User guestUser = userRepository.findByEmail(guest.user().email()).orElseThrow();
        createGamesList(guestUser, "Invité 1", 1);
        createGamesList(guestUser, "Invité 2", 2);

        ResponseEntity<Map> response = restTemplate.postForEntity(API_LOGIN,
                withToken(new LoginRequestDto(EMAIL, "mauvaisMotDePasse"), guest.token()), Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        // l'invité existe toujours, garde ses 2 listes et son token
        assertTrue(userRepository.existsById(guestUser.getId()));
        assertEquals(HttpStatus.OK, me("Bearer " + guest.token(), UserDto.class).getStatusCode());
        GamesListDto[] guestLists = getGamesLists(guest.token()).getBody();
        assertEquals(2, guestLists.length);
        assertEquals("Invité 1", guestLists[0].name());
        assertEquals(1, guestLists[0].position());
        assertEquals(2, guestLists[1].position());
        // le compte n'a reçu aucune liste
        String accountToken = login(EMAIL, PASSWORD, null).getBody().token();
        assertEquals(1, getGamesLists(accountToken).getBody().length);
    }

    @Test
    public void loginTest_vrai_compte_deja_connecte_ne_transfere_rien() {
        register("autre", "autre@test.com", PASSWORD);
        User other = userRepository.findByEmail("autre@test.com").orElseThrow();
        createGamesList(other, "Liste de l'autre", 1);
        String otherToken = login("autre@test.com", PASSWORD, null).getBody().token();
        register(USERNAME, EMAIL, PASSWORD);

        ResponseEntity<AuthResponseDto> response = login(EMAIL, PASSWORD, otherToken);

        // seul un invité est fusionné : un vrai compte garde ses listes et n'est pas supprimé
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(userRepository.existsById(other.getId()));
        assertEquals(0, getGamesLists(response.getBody().token()).getBody().length);
        assertEquals(1, getGamesLists(otherToken).getBody().length);
    }

    // ---------- outils ----------

    /** Connexion, avec le token d'un utilisateur déjà connecté (invité ou non) si {@code token} n'est pas null. */
    private ResponseEntity<AuthResponseDto> login(String email, String password, String token) {
        LoginRequestDto body = new LoginRequestDto(email, password);
        return restTemplate.postForEntity(API_LOGIN, token == null ? new HttpEntity<>(body) : withToken(body, token),
                AuthResponseDto.class);
    }

    private void createGamesList(User user, String name) {
        createGamesList(user, name, 1);
    }

    private void createGamesList(User user, String name, int position) {
        GamesList gamesList = new GamesList();
        gamesList.setName(name);
        gamesList.setPosition(position);
        gamesList.setGames(new ArrayList<>());
        gamesList.setUser(user);
        gamesListRepository.save(gamesList);
    }

    private ResponseEntity<GamesListDto[]> getGamesLists(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(API_GET_GAMES_LISTS, HttpMethod.GET, new HttpEntity<>(headers), GamesListDto[].class);
    }

    private <T> HttpEntity<T> withToken(T body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private ResponseEntity<AuthResponseDto> register(String username, String email, String password, String token) {
        return restTemplate.postForEntity(API_REGISTER,
                withToken(new RegisterRequestDto(username, email, password), token), AuthResponseDto.class);
    }

    private ResponseEntity<AuthResponseDto> guest() {
        return restTemplate.postForEntity(API_GUEST, null, AuthResponseDto.class);
    }

    private ResponseEntity<AuthResponseDto> register(String username, String email, String password) {
        return restTemplate.postForEntity(API_REGISTER,
                new RegisterRequestDto(username, email, password), AuthResponseDto.class);
    }

    private <T> ResponseEntity<T> me(String authorizationHeader, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        return restTemplate.exchange(API_ME, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    private void assertRegisterBadRequest(String username, String email, String password) {
        ResponseEntity<Map> response = restTemplate.postForEntity(API_REGISTER,
                new RegisterRequestDto(username, email, password), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0, userRepository.count());
    }
}
