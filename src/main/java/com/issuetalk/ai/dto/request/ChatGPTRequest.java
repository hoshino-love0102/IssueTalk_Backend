package com.issuetalk.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChatGPTRequest {
    private String model;
    private List<Message> messages;
    private double temperature = 0.7;

    public ChatGPTRequest(String model, String prompt) {
        this.model = model;
        this.messages = List.of(new Message("user", prompt));
    }

    public ChatGPTRequest(String model, String system, String prompt, double temperature) {
        this.model = model;
        this.temperature = temperature;
        this.messages = List.of(
                new Message("system", system),
                new Message("user", prompt)
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
