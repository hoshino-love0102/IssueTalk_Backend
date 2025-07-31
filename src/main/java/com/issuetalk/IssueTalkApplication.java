package com.issuetalk;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IssueTalkApplication {

    public static void main(String[] args) {
        // .env 값 로딩 후 시스템 프로퍼티로 등록
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));
        System.setProperty("NEWS_API_KEY", dotenv.get("NEWS_API_KEY"));

        SpringApplication.run(IssueTalkApplication.class, args);
    }
}
