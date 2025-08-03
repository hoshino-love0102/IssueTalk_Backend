package com.issuetalk.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    private String roomId; // 방 ID
    private List<String> teamAUserIds; // A팀 사용자 목록
    private List<String> teamBUserIds; // B팀 사용자 목록
}
