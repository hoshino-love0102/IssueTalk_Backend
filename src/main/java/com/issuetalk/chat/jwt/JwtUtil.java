package com.issuetalk.chat.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component // 스프링 빈으로 등록
public class JwtUtil {

    private final String secretKey = "your-secret-key"; // JWT 서명에 사용할 비밀 키

    // 토큰에서 사용자 ID 추출
    public String extractUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes()) // 서명 키 설정
                .parseClaimsJws(token) // 토큰 파싱
                .getBody(); // 페이로드 반환
        return claims.getSubject(); // subject (보통 userId) 추출
    }

    // 토큰에서 닉네임 추출
    public String extractNickname(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes()) // 서명 키 설정
                .parseClaimsJws(token) // 토큰 파싱
                .getBody(); // 페이로드 반환
        return claims.get("nickname", String.class); // nickname 클레임 추출
    }
}
