package com.stars.bigbang.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    private static final String SECRET = "test-only-secret-key-for-jwt-service-unit-tests-1234567890";
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;
    private static final long THREE_YEARS_MS = 94_672_800_000L;
    private static final String EMAIL = "test@test.com";

    private final JwtService jwtService = new JwtService(SECRET, ONE_HOUR_MS, THREE_YEARS_MS);

    @Test
    void accountTokenLastsTheAccountDuration() {
        assertEquals(ONE_HOUR_MS, lifetimeMs(jwtService.generateToken(EMAIL, 0)), 1000);
    }

    @Test
    void guestTokenLastsTheGuestDuration() {
        assertEquals(THREE_YEARS_MS, lifetimeMs(jwtService.generateGuestToken(EMAIL, 0)), 1000);
    }

    @Test
    void guestTokenStillIdentifiesTheUserByEmail() {
        assertEquals(EMAIL, jwtService.extractClaims(jwtService.generateGuestToken(EMAIL, 0)).getSubject());
    }

    @Test
    void accountTokenCarriesItsVersion() {
        String token = jwtService.generateToken(EMAIL, 3);

        assertEquals(3, jwtService.extractTokenVersion(jwtService.extractClaims(token)));
    }

    @Test
    void guestTokenCarriesItsVersion() {
        String token = jwtService.generateGuestToken(EMAIL, 2);

        assertEquals(2, jwtService.extractTokenVersion(jwtService.extractClaims(token)));
    }

    @Test
    void tokenWithoutVersionIsReadAsVersionZero() {
        // token émis avant SEC-03 : pas de claim « ver »
        String legacyToken = Jwts.builder()
                .subject(EMAIL)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertEquals(0, jwtService.extractTokenVersion(jwtService.extractClaims(legacyToken)));
    }

    /** Durée de vie du token. Un JWT stocke ses dates à la seconde près, d'où la tolérance d'1 s dans les tests. */
    private long lifetimeMs(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
    }
}
