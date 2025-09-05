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

    private final Map<String, ChatRoom> topicRoomMap = new ConcurrentHashMap<>();
    private final Map<String, Set<WebSocketSession>> teamSessions = new ConcurrentHashMap<>();

    public void saveRoom(String topicId, ChatRoom room) {
        topicRoomMap.put(topicId, room);
    }

    public Map<String, ChatRoom> getAllRooms() {
        return topicRoomMap;
    }

    public ChatRoom getRoomByRoomId(String roomId) {
        return topicRoomMap.values().stream()
                .filter(room -> room.getRoomId().equals(roomId))
                .findFirst()
                .orElse(null);
    }

    public void register(String teamId, WebSocketSession session) {
        teamSessions.computeIfAbsent(teamId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(String teamId, WebSocketSession session) {
        Set<WebSocketSession> sessions = teamSessions.get(teamId);
        if (sessions != null) sessions.remove(session);
    }

    public Set<WebSocketSession> getTeamSessions(String teamId) {
        return teamSessions.getOrDefault(teamId, Collections.emptySet());
    }
}
