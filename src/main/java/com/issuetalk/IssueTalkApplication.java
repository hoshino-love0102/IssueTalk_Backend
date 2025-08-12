package com.issuetalk;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IssueTalkApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // 필수 항목 목록
        requireAndSet("MONGO_URI", dotenv);
        requireAndSet("JWT_SECRET", dotenv);
        requireAndSet("OPENAI_API_KEY", dotenv);

        SpringApplication.run(IssueTalkApplication.class, args);
    }

    private static void requireAndSet(String key, Dotenv dotenv) {
        String value = firstNonBlank(System.getenv(key), dotenv.get(key));
        if (value == null || value.isBlank()) {
            System.err.printf("[FATAL] %s is required but missing. Set it as an environment variable or in your .env file.%n", key);
            System.exit(1); // fail-fast
        }
        System.setProperty(key, value);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
