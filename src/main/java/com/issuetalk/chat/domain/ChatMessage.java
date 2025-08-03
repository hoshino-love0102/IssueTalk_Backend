package com.issuetalk.chat.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chatMessages") // MongoDB 컬렉션 이름 설정
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    private String id;

    private String roomId; // 메시지가 속한 방 ID
    private String sender; // 보낸 사용자 아이디
    private String nickname; // 사용자 닉네임
    private String message; // 채팅 내용
    private boolean submitted; // 제출 여부 (true면 상대 팀에 전송)
    private LocalDateTime timestamp; // 보낸 시각
}
