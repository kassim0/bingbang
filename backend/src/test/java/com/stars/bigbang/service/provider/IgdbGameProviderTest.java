package com.stars.bigbang.service.provider;

import com.stars.bigbang.dto.igdbDto.IgdbCoverDto;
import com.stars.bigbang.dto.igdbDto.IgdbGameDto;
import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.igdb.IgdbApi;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IgdbGameProviderTest {

    @Mock
    private IgdbApi igdbApi;
    @Mock
    private IgdbTokenService tokenService;
    @InjectMocks
    private IgdbGameProvider provider;

    @BeforeEach
    void setUp() {
        when(tokenService.getClientId()).thenReturn("client");
        when(tokenService.getToken()).thenReturn("token");
    }

    @Test
    void mapsIgdbGameToSearchResult() {
        when(igdbApi.searchGames(any(), any(), any())).thenReturn(List.of(
                new IgdbGameDto(1022, "the-legend-of-zelda", "The Legend of Zelda", 509328000L, new IgdbCoverDto("co1uii"))));

        GameSearchResult result = provider.search("zelda").get(0);

        assertEquals(GameSource.IGDB, result.source());
        assertEquals(1022, result.externalId());
        assertEquals(LocalDate.of(1986, 2, 21), result.releaseDate());
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/co1uii.jpg", result.imageUrl());
    }

    @Test
    void gameWithoutCoverNorDateIsStillReturned() {
        when(igdbApi.searchGames(any(), any(), any())).thenReturn(List.of(new IgdbGameDto(1, "a", "A", null, null)));

        GameSearchResult result = provider.search("a").get(0);

        assertNull(result.imageUrl());
        assertNull(result.releaseDate());
    }

    @Test
    void userInputCannotAlterTheQuery() {
        when(igdbApi.searchGames(any(), any(), any())).thenReturn(List.of());

        provider.search("zelda\"; fields *; limit 500; \\");

        ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
        verify(igdbApi).searchGames(any(), any(), query.capture());
        assertTrue(query.getValue().startsWith("search \"zelda ; fields *; limit 500;\";"), query.getValue());
    }

    @Test
    void refusedTokenIsRenewedAndCallRetriedOnce() {
        Request request = Request.create(Request.HttpMethod.POST, "https://api.igdb.com/v4/games", Map.of(), null, null, null);
        when(igdbApi.searchGames(any(), any(), any()))
                .thenThrow(new FeignException.Unauthorized("401", request, null, null))
                .thenReturn(List.of());

        assertTrue(provider.search("zelda").isEmpty());

        verify(tokenService).invalidate();
        verify(igdbApi, times(2)).searchGames(any(), any(), any());
    }
}
