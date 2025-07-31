package com.issuetalk.jwt;

import com.issuetalk.user.entity.User;
import com.issuetalk.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expirationMillis;
    private final UserRepository userRepository;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") long expirationMillis,
                       UserRepository userRepository) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMillis;
        this.userRepository = userRepository;
    }

    // 토큰 생성 (username + nickname 포함)
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        // 닉네임 조회
        String nickname = userRepository.findByUsername(username)
                .map(User::getNickname)
                .orElse("익명");

        return Jwts.builder()
                .setSubject(username)
                .claim("nickname", nickname) // 닉네임 추가
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자명 추출
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(stripBearer(token))
                .getBody()
                .getSubject();
    }

    // 토큰에서 닉네임 추출(test)
    public String getNicknameFromToken(String token) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(stripBearer(token))
                .getBody()
                .get("nickname");
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(stripBearer(token));
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // "Bearer " 접두사 제거
    private String stripBearer(String token) {
        return (token != null && token.startsWith("Bearer "))
                ? token.substring(7)
                : token;
    }
}