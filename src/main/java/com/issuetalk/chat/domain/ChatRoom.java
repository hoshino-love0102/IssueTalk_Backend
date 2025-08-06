package com.issuetalk.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    private String roomId; // 채팅방 고유 ID (예: "abc123")
    private String topicId; // 토론 주제 ID 또는 제목 (예: "politics-001")
    private int maxTeamSize; // 팀당 정원 (예: 팀 A와 B 각각 최대 3명)
    private List<String> teamAUserIds = new ArrayList<>(); // 팀 A에 속한 유저들의 ID 목록
    private List<String> teamBUserIds = new ArrayList<>(); // 팀 B에 속한 유저들의 ID 목록
}
