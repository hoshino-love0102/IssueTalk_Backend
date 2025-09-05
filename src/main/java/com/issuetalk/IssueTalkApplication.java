package com.issuetalk;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class IssueTalkApplication {

    private static final String[] REQUIRED_KEYS = {
            "MONGO_URI",
            "JWT_SECRET",
            "OPENAI_API_KEY",
            "TAVILY_API_KEY"
    };

    private static final String[] OPTIONAL_KEYS = {
            "PORT"
    };

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        List<String> missing = new ArrayList<>();

        // 필수 키 로드 & 검증
        for (String key : REQUIRED_KEYS) {
            String value = firstNonBlank(System.getenv(key), dotenv.get(key));
            if (value == null || value.isBlank()) {
                missing.add(key);
            } else {
                System.setProperty(key, value);
            }
        }

        // 선택 키는 있으면 세팅
        for (String key : OPTIONAL_KEYS) {
            String value = firstNonBlank(System.getenv(key), dotenv.get(key));
            if (value != null && !value.isBlank()) {
                System.setProperty(key, value);
            }
        }

        if (!missing.isEmpty()) {
            System.err.println("[FATAL] Missing required environment variables:");
            missing.forEach(k -> System.err.println("  - " + k));
            System.err.println("Set them as real environment variables or in your .env file, then restart.");
            System.exit(1);
        }

        SpringApplication.run(IssueTalkApplication.class, args);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
