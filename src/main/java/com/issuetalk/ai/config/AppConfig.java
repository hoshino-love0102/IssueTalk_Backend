package com.issuetalk.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// 이 클래스는 스프링의 설정 클래스임을 나타냄 (@Configuration 사용)
@Configuration
public class AppConfig {

    // RestTemplate 객체를 빈으로 등록하여 다른 클래스에서 주입받아 사용할 수 있게 함
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(); // HTTP 요청을 간편하게 보낼 수 있는 Spring의 클라이언트 도구
    }
}