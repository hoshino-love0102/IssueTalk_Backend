package com.issuetalk.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChatGPTResponse {
    private List<Choice> choices; // GPT 응답의 선택지 목록

    @Getter
    @NoArgsConstructor
    public static class Choice {
        private Message message; // 응답 메시지 객체
    }

    @Getter
    @NoArgsConstructor
    public static class Message {
        private String role; // 역할 (user, assistant 등)
        private String content; // 응답 메시지 내용
    }
}
