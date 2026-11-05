package com.stars.bigbang.controller;

import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.service.GameSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Teste la recherche de jeux à travers la vraie configuration de sécurité :
 * elle doit rester accessible sans compte, comme le reste de la gestion des listes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameSearchControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private GameSearchService gameSearchService;

    @Test
    public void searchTest_sans_token() {
        when(gameSearchService.search("portal")).thenReturn(List.of(
                new GameSearchResult(GameSource.IGDB, 72, "portal-2", "Portal 2", null, "https://images.igdb.com/portal-2.jpg")));

        ResponseEntity<GameSearchResult[]> response = search("portal");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Portal 2", response.getBody()[0].name());
        assertEquals(GameSource.IGDB, response.getBody()[0].source());
    }

    @Test
    public void searchTest_nom_avec_espaces() {
        when(gameSearchService.search("the legend of zelda")).thenReturn(List.of());

        ResponseEntity<GameSearchResult[]> response = search("the legend of zelda");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gameSearchService).search("the legend of zelda");
    }

    private ResponseEntity<GameSearchResult[]> search(String name) {
        return restTemplate.getForEntity("/api/games/search?name={name}", GameSearchResult[].class, name);
    }
}
