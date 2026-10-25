package com.stars.bigbang.controller;

import com.stars.bigbang.dto.payload.LoginRequestDto;
import com.stars.bigbang.dto.payload.RegisterRequestDto;
import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.UserDto;
import com.stars.bigbang.entity.User;
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
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String API_REGISTER = "/api/auth/register";
    private static final String API_LOGIN = "/api/auth/login";
    private static final String API_ME = "/api/auth/me";

    private static final String USERNAME = "test";
    private static final String EMAIL = "test@test.com";
    private static final String PASSWORD = "password123";

    @BeforeEach
    public void cleanDatabase() {
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
        assertEquals(EMAIL, body.userDto().email());
        assertEquals(USERNAME, body.userDto().username());

        User newUser = userRepository.findByEmail(EMAIL).orElseThrow();
        assertEquals(USERNAME, newUser.getUsername());
        assertNotEquals(PASSWORD, newUser.getPassword());
        assertTrue(passwordEncoder.matches(PASSWORD, newUser.getPassword()));
    }

    @Test
    public void registerTest_email_normalise_en_minuscules() {
        ResponseEntity<AuthResponseDto> response = register(USERNAME, "TeSt@TeSt.CoM", PASSWORD);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(EMAIL, response.getBody().userDto().email());
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
        assertEquals(EMAIL, body.userDto().email());
        assertEquals(USERNAME, body.userDto().username());
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
        String expiredToken = new JwtService(jwtSecret, -1000).generateToken(EMAIL);

        assertEquals(HttpStatus.UNAUTHORIZED, me("Bearer " + expiredToken, Map.class).getStatusCode());
    }

    @Test
    public void meTest_cas_token_signe_avec_une_autre_cle() {
        register(USERNAME, EMAIL, PASSWORD);
        String forgedToken = new JwtService("une-autre-cle-secrete-qui-n-est-pas-celle-du-serveur-123456", 86400000)
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

    // ---------- outils ----------

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
