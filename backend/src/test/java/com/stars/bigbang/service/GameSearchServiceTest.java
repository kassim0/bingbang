package com.stars.bigbang.service;

import com.stars.bigbang.entity.AppSetting;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.repository.AppSettingRepository;
import com.stars.bigbang.service.provider.GameProvider;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameSearchServiceTest {

    @Mock
    private AppSettingRepository repository;
    @Mock
    private GameProvider igdb;
    @Mock
    private GameProvider rawg;

    private GameSearchService service;

    @BeforeEach
    void setUp() {
        when(igdb.source()).thenReturn(GameSource.IGDB);
        when(rawg.source()).thenReturn(GameSource.RAWG);
        service = new GameSearchService(repository, List.of(igdb, rawg));
    }

    private void settingIs(String value) {
        when(repository.findById("game_provider")).thenReturn(Optional.of(new AppSetting("game_provider", value)));
    }

    @Test
    void usesSourceDefinedInDatabase() {
        settingIs("rawg");
        service.search("zelda");
        verify(rawg).search("zelda");
        verify(igdb, never()).search(any());
    }

    @Test
    void settingIsReadOnEachSearch() {
        settingIs("RAWG");
        service.search("zelda");
        settingIs("IGDB");
        service.search("zelda");
        verify(rawg, times(1)).search("zelda");
        verify(igdb, times(1)).search("zelda");
    }

    @Test
    void missingOrUnknownSettingFallsBackToIgdb() {
        when(repository.findById("game_provider")).thenReturn(Optional.empty());
        service.search("zelda");
        settingIs("nimporte-quoi");
        service.search("zelda");
        verify(igdb, times(2)).search("zelda");
    }

    @Test
    void blankSearchCallsNoSource() {
        assertTrue(service.search("  ").isEmpty());
        verify(igdb, never()).search(any());
        verify(rawg, never()).search(any());
    }

    @Test
    void sourceFailureBecomesBadGateway() {
        settingIs("RAWG");
        Request request = Request.create(Request.HttpMethod.GET, "https://api.rawg.io/api/games", Map.of(), null, null, null);
        when(rawg.search(any())).thenThrow(new FeignException.InternalServerError("500", request, null, null));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.search("zelda"));

        assertEquals(HttpStatus.BAD_GATEWAY, e.getStatusCode());
    }
}
