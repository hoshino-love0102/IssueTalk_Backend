package com.issuetalk.auth.controller;

import com.issuetalk.auth.dto.LoginRequestDto;
import com.issuetalk.auth.dto.SignupRequestDto;
import com.issuetalk.auth.dto.TokenResponseDto;
import com.issuetalk.auth.service.AuthService;
import com.issuetalk.auth.dto.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // REST API 컨트롤러 선언
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService; // 인증 서비스 의존성 주입ㄷ

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login") // 로그인 요청 처리
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        TokenResponseDto token = authService.login(loginRequestDto);
        return ResponseEntity.ok(token); // JWT 토큰 반환
    }

    @PostMapping("/signup") // 회원가입 요청 처리
    public ResponseEntity<ApiResponseDto> signup(@RequestBody SignupRequestDto signupRequestDto) {
        authService.signup(signupRequestDto);
        return ResponseEntity.ok(new ApiResponseDto(true, "회원가입 성공")); // JSON 형식 메시지 반환
    }
}
