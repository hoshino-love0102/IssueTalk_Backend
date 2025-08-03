package com.issuetalk.chat;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionManager {

    // 팀 ID → 세션 목록
    private final Map<String, Set<WebSocketSession>> teamSessions = new ConcurrentHashMap<>();

    public void register(String teamId, WebSocketSession session) {
        teamSessions.computeIfAbsent(teamId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(String teamId, WebSocketSession session) {
        Set<WebSocketSession> sessions = teamSessions.get(teamId);
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    public Set<WebSocketSession> getTeamSessions(String teamId) {
        return teamSessions.getOrDefault(teamId, Collections.emptySet());
    }
}
