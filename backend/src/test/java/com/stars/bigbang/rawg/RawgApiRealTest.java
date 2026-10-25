package com.stars.bigbang.rawg;

import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Vérifie que le vrai RAWG répond toujours comme on l'attend (URL, clé, format du JSON).
 * Ignoré automatiquement si la variable d'environnement RAWG_API_KEY n'est pas définie :
 * les tests normaux ne doivent dépendre ni d'Internet, ni d'une clé, ni des quotas de RAWG.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RAWG_API_KEY", matches = ".+")
public class RawgApiRealTest {

    @DynamicPropertySource
    static void rawgProperties(DynamicPropertyRegistry registry) {
        registry.add("rawg.url", () -> "https://api.rawg.io/api");
        registry.add("bingbang.rawgApiSecret", () -> System.getenv("RAWG_API_KEY"));
    }

    @Autowired
    private RawgApi rawgApi;

    @Test
    public void searchGamesByNameTest_vrai_rawg() {
        RawgResponseDto response = rawgApi.searchGamesByName("The Legend of Zelda");

        assertTrue(response.getCount() > 0);
        assertFalse(response.getResults().isEmpty());
        assertTrue(response.getResults().stream()
                .anyMatch(game -> game.name().toLowerCase().contains("zelda")));
    }
}
