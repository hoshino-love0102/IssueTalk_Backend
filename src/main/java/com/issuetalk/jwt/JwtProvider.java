package com.issuetalk.jwt;

import com.issuetalk.user.entity.User;
import com.issuetalk.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component // Spring Bean으로 등록
public class JwtProvider {

    private final SecretKey key; // 서명에 사용할 비밀 키
    private final long expirationMillis; // 토큰 만료 시간(ms)
    private final UserRepository userRepository; // 사용자 정보 조회용

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") long expirationMillis,
                       UserRepository userRepository) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // 시크릿 키 초기화
        this.expirationMillis = expirationMillis;
        this.userRepository = userRepository;
    }

    // 토큰 생성 (username과 nickname 포함)
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        String nickname = userRepository.findByUsername(username)
                .map(User::getNickname)
                .orElse("익명");

        return Jwts.builder()
                .setSubject(username) // 사용자 이름을 subject에 저장
                .claim("nickname", nickname) // 닉네임을 클레임에 추가
                .setIssuedAt(now) // 발급 시각
                .setExpiration(expiry) // 만료 시각
                .signWith(key, SignatureAlgorithm.HS256) // 서명 알고리즘 및 키 설정
                .compact(); // 토큰 문자열 생성
    }

    // 토큰에서 username 추출
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(stripBearer(token))
                .getBody()
                .getSubject();
    }

    // 토큰에서 nickname 추출
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
