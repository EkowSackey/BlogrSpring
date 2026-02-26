package com.example.demo.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Key signingKey;
    private UserDetails userDetails;
    private final long EXPIRATION = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        byte[] secretBytes = new byte[32];
        for (int i = 0; i < secretBytes.length; i++) {
            secretBytes[i] = (byte) i;
        }
        String testSecret = Base64.getEncoder().encodeToString(secretBytes);
        signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret));

        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new User("testuser", "password", authorities);
    }

    @Test
    void generateJwtToken_shouldGenerateValidToken() {
        String token = jwtService.generateJwtToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void generateJwtToken_shouldSetCorrectSubject() {
        String token = jwtService.generateJwtToken(userDetails);
        String extractedSubject = jwtService.extractSubject(token);

        assertEquals(userDetails.getUsername(), extractedSubject);
    }

    @Test
    void generateJwtToken_shouldSetIssuedAtDate() {
        String token = jwtService.generateJwtToken(userDetails);
        Claims claims = jwtService.extractClaims(token);
        Date issuedAt = claims.getIssuedAt();

        assertNotNull(issuedAt);
    }

    @Test
    void generateJwtToken_shouldSetExpirationDate() {
        String token = jwtService.generateJwtToken(userDetails);
        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void generateJwtToken_shouldIncludeExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "admin");
        
        String token = jwtService.generateJwtToken(extraClaims, userDetails);
        Claims claims = jwtService.extractClaims(token);

        assertEquals("admin", claims.get("role"));
    }

    @Test
    void extractClaims_shouldExtractClaimsFromValidToken() {
        String token = jwtService.generateJwtToken(userDetails);
        Claims claims = jwtService.extractClaims(token);

        assertNotNull(claims);
        assertEquals(userDetails.getUsername(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void extractSubject_shouldExtractUsernameFromToken() {
        String token = jwtService.generateJwtToken(userDetails);
        String extractedUsername = jwtService.extractSubject(token);

        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenNotExpired() {
        String token = jwtService.generateJwtToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenExpired() {
        String expiredToken = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(signingKey)
                .compact();

        boolean isValid = jwtService.isTokenValid(expiredToken);
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenInvalid() {
        String invalidToken = "invalid.token.here";
        boolean isValid = jwtService.isTokenValid(invalidToken);
        assertFalse(isValid);
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
        String token = jwtService.generateJwtToken(userDetails);
        String[] parts = token.split("\\.").length == 3 ? token.split("\\.") : new String[3];
        String tamperedToken = parts[0] + "." + parts[1] + ".tampered";

        assertThrows(SignatureException.class, () -> {
            jwtService.extractClaims(tamperedToken);
        });
    }
}
