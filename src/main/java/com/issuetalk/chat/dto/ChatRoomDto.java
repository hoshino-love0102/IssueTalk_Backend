package com.issuetalk.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomDto {

    private String roomId; // 채팅방 ID
    private String topic; // 채팅 주제 (topic ID 또는 이름)
    private String lastMessage; // 마지막 메시지 내용
    private LocalDateTime lastUpdated; // 마지막 메시지 시간
}
