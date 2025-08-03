package com.issuetalk.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatPayload {
    private String roomId; // 채팅방 ID
    private String message; // 전송된 메시지
    private boolean submitted; // 제출 여부 (true: 최종 제출, false: 입력 중)
}
