package com.example.demo.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    Key generateSigningKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateJwtToken(UserDetails userDetails){
        return generateJwtToken(new HashMap<>(), userDetails);
    }

    public String generateJwtToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(generateSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String jwtToken){
        return Jwts.parserBuilder()
                .setSigningKey(generateSigningKey())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractSubject(String jwtToken){
        return extractClaim(jwtToken, Claims::getSubject);
    }

    public Date extractExpiration(String jwtToken){
        return extractClaim(jwtToken, Claims::getExpiration);
    }

    public boolean isTokenValid(String jwtToken){
        try {
            return new Date().before(extractExpiration(jwtToken));
        } catch (Exception e) {
            return false;
        }
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}
