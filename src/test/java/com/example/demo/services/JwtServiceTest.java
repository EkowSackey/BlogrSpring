package com.example.demo.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String testSecret;
    private Key signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        byte[] secretBytes = new byte[32];
        for (int i = 0; i < secretBytes.length; i++) {
            secretBytes[i] = (byte) i;
        }
        testSecret = Base64.getEncoder().encodeToString(secretBytes);
        signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret));

        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
    }

    @Test
    void generateJwtToken_shouldGenerateValidToken() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void generateJwtToken_shouldSetCorrectSubject() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);
        String extractedSubject = jwtService.extractSubject(token);

        assertEquals(username, extractedSubject);
    }

    @Test
    void generateJwtToken_shouldSetIssuedAtDate() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);
        Claims claims = jwtService.extractClaims(token);
        Date issuedAt = claims.getIssuedAt();

        assertNotNull(issuedAt);
        // Just verify it's set, don't check exact timing
    }

    @Test
    void generateJwtToken_shouldSetExpirationDate() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);
        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractClaims_shouldExtractClaimsFromValidToken() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);
        Claims claims = jwtService.extractClaims(token);

        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void extractSubject_shouldExtractUsernameFromToken() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);
        String extractedUsername = jwtService.extractSubject(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void extractSubject_shouldExtractDifferentUsernames() {
        String username1 = "user1";
        String username2 = "user2";
        String token1 = jwtService.generateJwtToken(username1);
        String token2 = jwtService.generateJwtToken(username2);

        String extracted1 = jwtService.extractSubject(token1);
        String extracted2 = jwtService.extractSubject(token2);

        assertEquals(username1, extracted1);
        assertEquals(username2, extracted2);
        assertNotEquals(extracted1, extracted2);
    }

    @Test
    void extractExpiration_shouldExtractExpirationDate() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);
        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenNotExpired() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);
        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_shouldThrowException_whenTokenExpired() {
        String username = "testuser";
        String expiredToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(signingKey)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(expiredToken);
        });
    }

    @Test
    void extractClaims_shouldThrowException_whenTokenInvalid() {
        String invalidToken = "invalid.token.here";

        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractClaims(invalidToken);
        });
    }

    @Test
    void extractClaims_shouldThrowException_whenSignatureInvalid() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tampered";

        assertThrows(SignatureException.class, () -> {
            jwtService.extractClaims(tamperedToken);
        });
    }

    @Test
    void extractClaims_shouldThrowException_whenTokenExpired() {
        String username = "testuser";
        String expiredToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(signingKey)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.extractClaims(expiredToken);
        });
    }

    @Test
    void extractSubject_shouldHandleSpecialCharacters() {
        String username = "user@example.com";
        String token = jwtService.generateJwtToken(username);
        String extractedUsername = jwtService.extractSubject(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void generateJwtToken_shouldCreateTokenWithCorrectExpiration() {
        String username = "testuser";
        long expectedDuration = 1000 * 24 * 24;
        long beforeGeneration = System.currentTimeMillis();

        String token = jwtService.generateJwtToken(username);
        Date expiration = jwtService.extractExpiration(token);
        long actualDuration = expiration.getTime() - beforeGeneration;

        // Allow 2 second tolerance
        assertTrue(Math.abs(actualDuration - expectedDuration) < 2000);
    }

    @Test
    void isTokenValid_shouldValidateTokenCorrectly() {
        String username = "testuser";
        String validToken = jwtService.generateJwtToken(username);

        boolean isValid = jwtService.isTokenValid(validToken);
        String extractedUsername = jwtService.extractSubject(validToken);

        assertTrue(isValid);
        assertEquals(username, extractedUsername);
    }

    @Test
    void generateJwtToken_shouldHandleNullUsername() {
        String username = null;

        assertDoesNotThrow(() -> {
            String token = jwtService.generateJwtToken(username);
            assertNotNull(token);
        });
    }

    @Test
    void generateJwtToken_shouldSetBothIssuedAtAndExpiration() {
        String username = "testuser";
        String token = jwtService.generateJwtToken(username);
        Claims claims = jwtService.extractClaims(token);

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void extractSubject_shouldReturnNullForNullSubject() {
        String token = Jwts.builder()
                .setSubject(null)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(signingKey)
                .compact();

        String extractedSubject = jwtService.extractSubject(token);
        assertNull(extractedSubject);
    }
}
