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
        System.out.println("[로그인 요청] userId: " + request.getUserId());

        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> {
                    System.out.println("[로그인 실패] 존재하지 않는 아이디");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디가 존재하지 않습니다.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("[로그인 실패] 비밀번호 불일치");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.generateToken(user.getUserId());
        System.out.println("[로그인 성공] userId: " + user.getUserId() + ", nickname: " + user.getNickname());
        return new TokenResponseDto(token, user.getNickname());
    }

    // 회원가입 및 토큰 반환
    public String signupAndGetToken(SignupRequestDto requestDto) {
        System.out.println("[회원가입 요청] userId: " + requestDto.getUserId());

        if (userRepository.existsByUserId(requestDto.getUserId())) {
            System.out.println("[회원가입 실패] 아이디 중복");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다.");
        }

        String encryptedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User(
                requestDto.getUserId(),
                encryptedPassword,
                requestDto.getNickname()
        );

        userRepository.save(user);
        System.out.println("[회원가입 성공] userId: " + user.getUserId());

        return jwtProvider.generateToken(user.getUserId());
    }
}
