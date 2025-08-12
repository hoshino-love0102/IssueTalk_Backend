package com.issuetalk;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IssueTalkApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String mongoUri = dotenv.get("MONGO_URI");
        if (mongoUri != null && !mongoUri.isBlank()) {
            System.setProperty("MONGO_URI", mongoUri);
        }

        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));

        SpringApplication.run(IssueTalkApplication.class, args);
    }
}
