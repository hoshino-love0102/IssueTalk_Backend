package com.issuetalk.auth.service;

import com.issuetalk.auth.dto.LoginRequestDto;
import com.issuetalk.auth.dto.SignupRequestDto;
import com.issuetalk.auth.dto.TokenResponseDto;
import com.issuetalk.jwt.JwtProvider;
import com.issuetalk.user.entity.User;
import com.issuetalk.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    // 로그인 처리 및 JWT 토큰 반환
    public TokenResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.generateToken(user.getUserId());
        return new TokenResponseDto(token, user.getNickname());
    }

    public String signupAndGetToken(SignupRequestDto requestDto) {
        if (userRepository.existsByUserId(requestDto.getUserId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        String encryptedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User(
                requestDto.getUserId(),
                encryptedPassword,
                requestDto.getNickname()
        );

        userRepository.save(user);

        return jwtProvider.generateToken(user.getUserId());
    }
}
