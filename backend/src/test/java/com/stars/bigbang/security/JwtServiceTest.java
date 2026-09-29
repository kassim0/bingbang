package com.stars.bigbang.security;

import com.stars.bigbang.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-please-override-me-1234567890abcdef";

    private JwtService jwtService;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60_000, 120_000);
        principal = new UserPrincipal(new User("alice", "alice@example.com", "hashed"));
    }

    @Test
    void generatesTokenContainingUserEmailAsSubject() {
        String token = jwtService.generateToken(principal);

        assertEquals("alice@example.com", jwtService.extractUsername(token));
    }

    @Test
    void validatesTokenForMatchingUser() {
        String token = jwtService.generateToken(principal);

        assertTrue(jwtService.isTokenValid(token, principal));
    }

    @Test
    void rejectsTokenForDifferentUser() {
        String token = jwtService.generateToken(principal);
        UserPrincipal otherUser = new UserPrincipal(new User("bob", "bob@example.com", "hashed"));

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void rejectsExpiredToken() {
        JwtService shortLivedJwtService = new JwtService(SECRET, -1, 120_000);
        String token = shortLivedJwtService.generateToken(principal);

        assertThrows(ExpiredJwtException.class, () -> shortLivedJwtService.isTokenValid(token, principal));
    }

    @Test
    void guestTokenUsesTheGuestExpiration() {
        JwtService shortLivedGuestJwtService = new JwtService(SECRET, 120_000, -1);
        String guestToken = shortLivedGuestJwtService.generateGuestToken(principal);

        assertThrows(ExpiredJwtException.class, () -> shortLivedGuestJwtService.isTokenValid(guestToken, principal));
    }
}
