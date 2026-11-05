package com.stars.bigbang.service.provider;

import com.stars.bigbang.dto.igdbDto.TwitchTokenDto;
import com.stars.bigbang.igdb.TwitchAuthApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Fournit le token d'accès à IGDB, délivré par Twitch. Il est valable plusieurs semaines :
 * on le garde en mémoire et on n'en redemande un qu'à son expiration.
 */
@Service
@RequiredArgsConstructor
public class IgdbTokenService {

    private static final long EXPIRATION_MARGIN_SECONDS = 60;

    private final TwitchAuthApi twitchAuthApi;

    @Value("${igdb.client-id}")
    private String clientId;

    @Value("${igdb.client-secret}")
    private String clientSecret;

    private String token;
    private Instant expiresAt = Instant.MIN;

    public synchronized String getToken() {
        if (token == null || Instant.now().isAfter(expiresAt)) {
            TwitchTokenDto response = twitchAuthApi.getToken(Map.of(
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "grant_type", "client_credentials"
            ));
            token = response.access_token();
            expiresAt = Instant.now().plusSeconds(response.expires_in() - EXPIRATION_MARGIN_SECONDS);
        }
        return token;
    }

    /** À appeler quand IGDB refuse le token, pour forcer la demande d'un nouveau. */
    public synchronized void invalidate() {
        token = null;
    }

    public String getClientId() {
        return clientId;
    }
}
