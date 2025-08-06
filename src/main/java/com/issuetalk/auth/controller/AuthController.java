package com.issuetalk.auth.controller;

import com.issuetalk.auth.dto.LoginRequestDto;
import com.issuetalk.auth.dto.SignupRequestDto;
import com.issuetalk.auth.dto.TokenResponseDto;
import com.issuetalk.auth.service.AuthService;
import com.issuetalk.auth.dto.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        TokenResponseDto token = authService.login(loginRequestDto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto> signup(@RequestBody SignupRequestDto signupRequestDto) {
        // 회원가입 처리 후 토큰 발급
        String token = authService.signupAndGetToken(signupRequestDto);

        return ResponseEntity.ok(new ApiResponseDto(true, "회원가입 성공", token));
    }
}
