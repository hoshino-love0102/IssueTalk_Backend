package com.issuetalk.auth.service;

import com.issuetalk.auth.dto.LoginRequestDto;
import com.issuetalk.auth.dto.SignupRequestDto;
import com.issuetalk.auth.dto.TokenResponseDto;
import com.issuetalk.jwt.JwtProvider;
import com.issuetalk.user.entity.User;
import com.issuetalk.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // 서비스 계층으로 등록
public class AuthService {

    private final UserRepository userRepository; // 사용자 데이터 접근
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 도구
    private final JwtProvider jwtProvider; // JWT 토큰 생성기

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    // 로그인 처리 및 JWT 토큰 반환
    public TokenResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.generateToken(user.getUsername());
        return new TokenResponseDto(token);
    }

    // 회원가입 처리
    public void signup(SignupRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        String encryptedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User(
                requestDto.getUsername(),
                encryptedPassword,
                requestDto.getNickname(),
                requestDto.getBirthDate()
        );

        userRepository.save(user);
    }
}
