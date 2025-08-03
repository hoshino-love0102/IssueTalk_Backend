package com.issuetalk.chat.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatPayload {
    private String roomId;
    private String message;
    private boolean submitted; // 여기를 추가!
}
