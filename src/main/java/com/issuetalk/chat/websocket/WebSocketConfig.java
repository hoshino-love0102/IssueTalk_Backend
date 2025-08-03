package com.issuetalk.chat.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler handler;

    public WebSocketConfig(ChatWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/chat")
                .setAllowedOrigins("*"); // CORS와 별도로 WebSocket 자체에도 허용 설정 필요
    }
}