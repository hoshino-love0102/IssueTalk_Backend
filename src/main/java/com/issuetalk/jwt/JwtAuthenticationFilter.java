package com.issuetalk.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request); // 요청 헤더에서 토큰 추출

        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) { // 토큰 존재 및 유효성 검사
            String username = jwtProvider.getUsernameFromToken(token); // 토큰에서 사용자 이름 추출
            // 인증 객체 생성 (권한은 비워둠)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, null);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // 요청 정보 설정

            SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 정보 저장
        }

        filterChain.doFilter(request, response); // 다음 필터로 요청 전달
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION); // Authorization 헤더 가져오기
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7); // "Bearer " 제거하고 토큰만 추출
        }
        return null;
    }
}