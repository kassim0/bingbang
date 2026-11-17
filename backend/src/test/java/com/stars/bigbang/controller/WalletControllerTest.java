package com.stars.bigbang.controller;

import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.WalletDto;
import com.stars.bigbang.entity.WalletBackground;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.UserRepository;
import com.stars.bigbang.repository.WalletRepository;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WalletControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GamesListRepository gamesListRepository;
    @Autowired
    private WalletRepository walletRepository;

    private String token;

    @BeforeEach
    public void setUp() {
        gamesListRepository.deleteAll();
        userRepository.deleteAll(); // la base supprime les wallets avec leur compte
        token = newGuestToken();
    }

    @Test
    public void getTest_fond_par_defaut_sans_rien_creer_en_base() {
        ResponseEntity<WalletDto> response = get(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(WalletBackground.COSMIC, response.getBody().background());
        assertEquals(0, walletRepository.count());
    }

    @Test
    public void setBackgroundTest_cas_nominal() {
        ResponseEntity<WalletDto> response = set(token, Map.of("background", "AURORA"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(WalletBackground.AURORA, response.getBody().background());
        assertEquals(WalletBackground.AURORA, get(token).getBody().background());
    }

    @Test
    public void setBackgroundTest_changer_plusieurs_fois_garde_un_seul_wallet() {
        set(token, Map.of("background", "AURORA"));
        set(token, Map.of("background", "sunset")); // la casse n'a pas d'importance

        assertEquals(WalletBackground.SUNSET, get(token).getBody().background());
        assertEquals(1, walletRepository.count());
    }

    @Test
    public void setBackgroundTest_cas_fond_inconnu_ou_absent() {
        assertEquals(HttpStatus.BAD_REQUEST, set(token, Map.of("background", "https://evil.example/bg.png")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, set(token, Map.of("background", " ")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, set(token, Map.of()).getStatusCode());
        assertEquals(WalletBackground.COSMIC, get(token).getBody().background());
    }

    @Test
    public void chaque_utilisateur_a_son_propre_fond() {
        String otherToken = newGuestToken();

        set(token, Map.of("background", "FOREST"));

        assertEquals(WalletBackground.FOREST, get(token).getBody().background());
        assertEquals(WalletBackground.COSMIC, get(otherToken).getBody().background());
    }

    @Test
    public void sans_token_est_refuse() {
        assertEquals(HttpStatus.UNAUTHORIZED, restTemplate.getForEntity("/api/wallet", String.class).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, restTemplate.exchange("/api/wallet/background", HttpMethod.PUT,
                new HttpEntity<>(Map.of("background", "AURORA")), String.class).getStatusCode());
    }

    private String newGuestToken() {
        return restTemplate.postForEntity("/api/auth/guest", null, AuthResponseDto.class).getBody().token();
    }

    private ResponseEntity<WalletDto> get(String token) {
        return restTemplate.exchange("/api/wallet", HttpMethod.GET, new HttpEntity<>(headers(token)), WalletDto.class);
    }

    private ResponseEntity<WalletDto> set(String token, Map<String, Object> body) {
        return restTemplate.exchange("/api/wallet/background", HttpMethod.PUT, new HttpEntity<>(body, headers(token)), WalletDto.class);
    }

    private HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
