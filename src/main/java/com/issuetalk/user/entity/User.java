package com.issuetalk.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String password;
    private String nickname;
    private LocalDate birthDate;

    @Builder
    public User(String username, String password, String nickname, LocalDate birthDate) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.birthDate = birthDate;
    }
}
