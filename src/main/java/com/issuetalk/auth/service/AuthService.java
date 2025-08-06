package com.issuetalk.auth.service;

import com.issuetalk.auth.dto.LoginRequestDto;
import com.issuetalk.auth.dto.SignupRequestDto;
import com.issuetalk.auth.dto.TokenResponseDto;
import com.issuetalk.jwt.JwtProvider;
import com.issuetalk.user.entity.User;
import com.issuetalk.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "아이디가 존재하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.generateToken(user.getUserId());
        return new TokenResponseDto(token, user.getNickname());
    }

    // 회원가입 및 토큰 반환
    public String signupAndGetToken(SignupRequestDto requestDto) {
        if (userRepository.existsByUserId(requestDto.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "이미 존재하는 아이디입니다.");
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
