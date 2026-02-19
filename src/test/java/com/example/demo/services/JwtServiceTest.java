package com.example.demo.services;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    // A 256-bit secret key encoded in Base64
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
    }

    @Test
    void generateJwtToken_ShouldReturnValidToken() {
        String username = "testUser";
        String token = jwtService.generateJwtToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractSubject_ShouldReturnCorrectUsername() {
        String username = "testUser";
        String token = jwtService.generateJwtToken(username);

        String extractedUsername = jwtService.extractSubject(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate() {
        String token = jwtService.generateJwtToken("testUser");

        Date expiration = jwtService.extractExpiration(token);

        assertTrue(expiration.after(new Date()));
    }

    @Test
    void isTokenValid_WhenNotExpired_ShouldReturnTrue() {
        String token = jwtService.generateJwtToken("testUser");

        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void extractClaims_ShouldReturnAllClaims() {
        String username = "testUser";
        String token = jwtService.generateJwtToken(username);

        Claims claims = jwtService.extractClaims(token);

        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}
