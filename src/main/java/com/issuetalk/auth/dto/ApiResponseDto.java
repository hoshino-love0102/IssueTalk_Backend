package com.issuetalk.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponseDto {
    private boolean success; // 요청 성공 여부
    private String message; // 응답 메시지
    private String token; // JWT 토큰
}
