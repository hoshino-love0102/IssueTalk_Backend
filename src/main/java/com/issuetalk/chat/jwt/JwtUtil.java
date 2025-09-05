package com.issuetalk.chat.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractNickname(String token) {
        return parseClaims(token).get("nickname", String.class);
    }

    public String extractUserIdFromBearer(String bearerToken) {
        return extractUserId(stripBearer(bearerToken));
    }

    public String extractNicknameFromBearer(String bearerToken) {
        return extractNickname(stripBearer(bearerToken));
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String stripBearer(String token) {
        return (token != null && token.startsWith("Bearer ")) ? token.substring(7) : token;
    }
}