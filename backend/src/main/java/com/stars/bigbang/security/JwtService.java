package com.stars.bigbang.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;
    private final long expirationGuestMs;

    public JwtService(@Value("${jwt.secret}") String key,
                      @Value("${jwt.expiration-ms}") long expirationMs,
                      @Value("${jwt.expiration-guest-ms}") long expirationGuestMs) {
        this.key = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.expirationGuestMs = expirationGuestMs;
    }

    private String generateToken(String email, long durationMs){
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime()+durationMs))
                .signWith(key)
                .compact();
    }

    public String generateToken(String email){
        return generateToken(email,expirationMs);
    }

    public String generateGuestToken(String email) {
        return generateToken(email,expirationGuestMs);
    }

    /**
     * Lève une exception JwtException si token invalide
     * */
    private Claims extractClaims(String token){
        return  Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token) //check la signature, format, expiration
                    .getPayload();
    }

    public String extractEmail(String token){
        Claims tokenPayload = extractClaims(token);
        return tokenPayload.getSubject();
    }


}
