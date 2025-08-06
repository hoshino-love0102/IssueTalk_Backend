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
        System.out.println("[요청] /auth/login - userId: " + loginRequestDto.getUserId());
        TokenResponseDto token = authService.login(loginRequestDto);
        System.out.println("[응답] /auth/login - nickname: " + token.getNickname());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto> signup(@RequestBody SignupRequestDto signupRequestDto) {
        System.out.println("[요청] /auth/signup - userId: " + signupRequestDto.getUserId());
        String token = authService.signupAndGetToken(signupRequestDto);
        System.out.println("[응답] /auth/signup - token 생성됨");
        return ResponseEntity.ok(new ApiResponseDto(true, "회원가입 성공", token));
    }
}
