package com.issuetalk.chat.service;

import com.issuetalk.chat.domain.ChatRoom;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionManager {

    // 토픽 ID별 채팅방 정보 저장
    private final Map<String, ChatRoom> topicRoomMap = new ConcurrentHashMap<>();

    // 팀 ID별 WebSocket 세션 집합
    private final Map<String, Set<WebSocketSession>> teamSessions = new ConcurrentHashMap<>();

    // 채팅방 저장
    public void saveRoom(String topicId, ChatRoom room) {
        topicRoomMap.put(topicId, room);
    }

    // 모든 채팅방 조회 (topicId → ChatRoom)
    public Map<String, ChatRoom> getAllRooms() {
        return topicRoomMap;
    }

    // 세션 등록 (팀 단위)
    public void register(String teamId, WebSocketSession session) {
        teamSessions.computeIfAbsent(teamId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    // 세션 해제 (연결 종료 시)
    public void unregister(String teamId, WebSocketSession session) {
        Set<WebSocketSession> sessions = teamSessions.get(teamId);
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    // 팀 ID로 세션 목록 조회
    public Set<WebSocketSession> getTeamSessions(String teamId) {
        return teamSessions.getOrDefault(teamId, Collections.emptySet());
    }

    // roomId 기준으로 ChatRoom 조회
    public ChatRoom getRoomByRoomId(String roomId) {
        for (ChatRoom room : topicRoomMap.values()) {
            if (room.getRoomId().equals(roomId)) {
                return room;
            }
        }
        return null;
    }

}
