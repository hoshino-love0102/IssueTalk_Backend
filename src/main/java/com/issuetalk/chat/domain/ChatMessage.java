package com.issuetalk.chat;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chatMessages")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    private String id;
    private String roomId;
    private String sender;     // username
    private String nickname;   // 닉네임 (토큰에서 추출)
    private String message;
    private boolean submitted;
    private LocalDateTime timestamp;
}
