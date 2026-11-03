package com.stars.bigbang.rawg;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Teste le client Feign RAWG contre un faux serveur HTTP (WireMock) qui imite RAWG :
 * aucune connexion Internet ni vraie clé n'est nécessaire.
 */
@SpringBootTest
public class RawgApiTest {

    @RegisterExtension
    static WireMockExtension rawgServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    // le client Feign appelle le faux serveur au lieu de https://api.rawg.io/api
    @DynamicPropertySource
    static void rawgProperties(DynamicPropertyRegistry registry) {
        registry.add("rawg.url", rawgServer::baseUrl);
    }

    @Autowired
    private RawgApi rawgApi;

    // valeur définie dans src/test/resources/application.properties
    private static final String RAWG_KEY = "test-rawg-key";

    @Test
    public void searchGamesByNameTest_cas_nominal() throws IOException {
        rawgServer.stubFor(get(urlPathEqualTo("/games"))
                .willReturn(okJson(readJson("rawg/search-zelda.json"))));

        RawgResponseDto response = rawgApi.searchGamesByName("zelda");

        assertEquals(2, response.getCount());
        assertEquals(2, response.getResults().size());
        RawgResultsDto firstGame = response.getResults().get(0);
        assertEquals(22511, firstGame.id());
        assertEquals("the-legend-of-zelda-breath-of-the-wild", firstGame.slug());
        assertEquals("The Legend of Zelda: Breath of the Wild", firstGame.name());
        assertEquals("2017-03-02", firstGame.released());
        assertFalse(firstGame.tba());
        assertEquals("https://media.rawg.io/media/games/cc1/cc196a5ad763955d6532cdba236f730c.jpg", firstGame.background_image());
    }

    @Test
    public void searchGamesByNameTest_cle_ajoutee_par_l_intercepteur() throws IOException {
        rawgServer.stubFor(get(urlPathEqualTo("/games"))
                .willReturn(okJson(readJson("rawg/search-zelda.json"))));

        rawgApi.searchGamesByName("The Legend of Zelda");

        rawgServer.verify(1, getRequestedFor(urlPathEqualTo("/games"))
                .withQueryParam("search", equalTo("The Legend of Zelda"))
                .withQueryParam("key", equalTo(RAWG_KEY)));
    }

    @Test
    public void searchGamesByNameTest_cas_aucun_resultat() {
        rawgServer.stubFor(get(urlPathEqualTo("/games"))
                .willReturn(okJson("{\"count\": 0, \"next\": null, \"previous\": null, \"results\": []}")));

        RawgResponseDto response = rawgApi.searchGamesByName("jeuquinexistepas");

        assertEquals(0, response.getCount());
        assertEquals(0, response.getResults().size());
    }

    @Test
    public void searchGamesByNameTest_cas_cle_refusee_par_rawg() {
        rawgServer.stubFor(get(urlPathEqualTo("/games"))
                .willReturn(unauthorized().withBody("{\"error\": \"The key parameter is not provided\"}")));

        assertThrows(FeignException.Unauthorized.class, () -> rawgApi.searchGamesByName("zelda"));
    }

    @Test
    public void searchGamesByNameTest_cas_rawg_en_erreur() {
        rawgServer.stubFor(get(urlPathEqualTo("/games"))
                .willReturn(serverError()));

        assertThrows(FeignException.InternalServerError.class, () -> rawgApi.searchGamesByName("zelda"));
    }

    private String readJson(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}
