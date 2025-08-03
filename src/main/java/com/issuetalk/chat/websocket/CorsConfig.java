package com.issuetalk.chat.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // WebSocket handshake에 필요한 CORS 설정
                .allowedOrigins("*")   // 모든 출처 허용(임시)
                .allowedMethods("*")
                .allowCredentials(true);
    }
}