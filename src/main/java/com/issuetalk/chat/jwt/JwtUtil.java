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

    @Value("${jwt.secret}") // application.properties에서 주입
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 토큰에서 userId(subject) 추출
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰에서 nickname 추출
    public String extractNickname(String token) {
        return parseClaims(token).get("nickname", String.class);
    }

    // "Bearer " 포함된 토큰에서 userId 추출
    public String extractUserIdFromBearer(String bearerToken) {
        return extractUserId(stripBearer(bearerToken));
    }

    // "Bearer " 포함된 토큰에서 nickname 추출
    public String extractNicknameFromBearer(String bearerToken) {
        return extractNickname(stripBearer(bearerToken));
    }

    // 내부적으로 Claims 파싱 처리
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // "Bearer " 제거
    public String stripBearer(String token) {
        return (token != null && token.startsWith("Bearer ")) ? token.substring(7) : token;
    }
}