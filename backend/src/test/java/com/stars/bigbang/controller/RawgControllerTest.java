package com.stars.bigbang.controller;

import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.rawg.RawgApi;
import com.stars.bigbang.repository.UserRepository;
import com.stars.bigbang.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Teste RawgController sans aucun appel HTTP vers RAWG :
 * le client Feign est remplacé par un mock Mockito dont on choisit la réponse.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RawgControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

    @MockBean
    private RawgApi rawgApi;

    private static final String API_RAWG = "/api/rawg/";
    private static final String EMAIL = "rawg@test.com";

    private String token;

    @BeforeEach
    public void createUser() {
        userRepository.deleteAll();
        User user = new User();
        user.setUsername("rawg");
        user.setEmail(EMAIL);
        user.setPassword("non-utilise-dans-ces-tests");
        userRepository.save(user);
        token = jwtService.generateToken(EMAIL);
    }

    @Test
    public void getJeuxByNameTest_cas_nominal() {
        RawgResponseDto rawgResponse = new RawgResponseDto();
        rawgResponse.setCount(1);
        rawgResponse.setResults(List.of(new RawgResultsDto(4200, "portal-2", "Portal 2", "2011-04-18", false, "https://media.rawg.io/portal-2.jpg")));
        when(rawgApi.searchGamesByName("portal")).thenReturn(rawgResponse);

        ResponseEntity<RawgResponseDto> response = getJeuxByName("portal");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getCount());
        assertEquals("Portal 2", response.getBody().getResults().get(0).name());
        verify(rawgApi).searchGamesByName("portal");
    }

    @Test
    public void getJeuxByNameTest_nom_avec_espaces() {
        RawgResponseDto rawgResponse = new RawgResponseDto();
        rawgResponse.setResults(List.of());
        when(rawgApi.searchGamesByName("the legend of zelda")).thenReturn(rawgResponse);

        ResponseEntity<RawgResponseDto> response = getJeuxByName("the legend of zelda");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(rawgApi).searchGamesByName("the legend of zelda");
    }

    private ResponseEntity<RawgResponseDto> getJeuxByName(String gameName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(API_RAWG + "{gameName}", HttpMethod.GET, new HttpEntity<>(headers),
                RawgResponseDto.class, gameName);
    }
}
