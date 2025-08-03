package com.issuetalk.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor // 기본 생성자
public class ChatGPTRequest {

    private String model; // 사용할 GPT 모델 이름
    private List<Message> messages; // 메시지 목록

    public ChatGPTRequest(String model, String prompt) {
        this.model = model;
        this.messages = List.of(new Message("user", prompt)); // 사용자 메시지 하나로 구성
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role; // 메시지 역할 (user, assistant 등)
        private String content; // 메시지 내용
    }
}
