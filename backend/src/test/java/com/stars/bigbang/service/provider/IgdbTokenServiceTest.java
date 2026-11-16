package com.stars.bigbang.service.provider;

import com.stars.bigbang.dto.igdbDto.TwitchTokenDto;
import com.stars.bigbang.igdb.TwitchAuthApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IgdbTokenServiceTest {

    @Mock
    private TwitchAuthApi twitchAuthApi;

    private IgdbTokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new IgdbTokenService(twitchAuthApi);
        // ces deux champs sont normalement remplis par Spring (@Value)
        ReflectionTestUtils.setField(tokenService, "clientId", "my-client");
        ReflectionTestUtils.setField(tokenService, "clientSecret", "my-secret");
    }

    @Test
    void tokenIsRequestedOnceThenKeptInMemory() {
        when(twitchAuthApi.getToken(any())).thenReturn(new TwitchTokenDto("token-1", 3600));

        assertEquals("token-1", tokenService.getToken());
        assertEquals("token-1", tokenService.getToken());

        verify(twitchAuthApi, times(1)).getToken(any());
    }

    @Test
    void tokenRequestCarriesTheClientCredentials() {
        when(twitchAuthApi.getToken(any())).thenReturn(new TwitchTokenDto("token-1", 3600));

        tokenService.getToken();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, ?>> form = ArgumentCaptor.forClass(Map.class);
        verify(twitchAuthApi).getToken(form.capture());
        assertEquals(Map.of("client_id", "my-client", "client_secret", "my-secret", "grant_type", "client_credentials"), form.getValue());
    }

    @Test
    void expiredTokenIsRenewed() {
        // 30 secondes de validité, moins la marge de sécurité de 60 secondes : déjà considéré comme expiré
        when(twitchAuthApi.getToken(any()))
                .thenReturn(new TwitchTokenDto("short-lived", 30))
                .thenReturn(new TwitchTokenDto("token-2", 3600));

        assertEquals("short-lived", tokenService.getToken());
        assertEquals("token-2", tokenService.getToken());
    }

    @Test
    void invalidatedTokenIsRenewed() {
        when(twitchAuthApi.getToken(any()))
                .thenReturn(new TwitchTokenDto("token-1", 3600))
                .thenReturn(new TwitchTokenDto("token-2", 3600));
        tokenService.getToken();

        tokenService.invalidate();

        assertEquals("token-2", tokenService.getToken());
    }
}
