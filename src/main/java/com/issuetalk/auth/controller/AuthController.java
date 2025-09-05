package com.issuetalk.auth.controller;

import com.issuetalk.auth.dto.LoginRequestDto;
import com.issuetalk.auth.dto.SignupRequestDto;
import com.issuetalk.auth.dto.TokenResponseDto;
import com.issuetalk.auth.dto.ApiResponseDto;
import com.issuetalk.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto req) {
        System.out.println("[AUTH][LOGIN] userId=" + req.getUserId());
        TokenResponseDto token = authService.login(req);
        System.out.println("[AUTH][LOGIN] nickname=" + token.getNickname());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto> signup(@RequestBody SignupRequestDto req) {
        System.out.println("[AUTH][SIGNUP] userId=" + req.getUserId());
        String token = authService.signupAndGetToken(req);
        System.out.println("[AUTH][SIGNUP] token issued");
        return ResponseEntity.ok(new ApiResponseDto(true, "회원가입 성공", token));
    }
}
