package com.issuetalk.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetalk.chat.domain.ChatMessage;
import com.issuetalk.chat.dto.ChatPayload;
import com.issuetalk.chat.jwt.JwtUtil;
import com.issuetalk.chat.repository.ChatMessageRepository;
import com.issuetalk.chat.service.ChatSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageRepository messageRepository; // 메시지 저장소
    private final JwtUtil jwtUtil; // JWT 유틸
    private final ChatSessionManager sessionManager; // 세션 관리자
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파서

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String teamId = getTeamId(session);
        if (teamId != null) {
            sessionManager.register(teamId, session);
        }
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
        ChatPayload payload = objectMapper.readValue(message.getPayload(), ChatPayload.class);

        String token = extractToken(session);
        String userId = jwtUtil.extractUserId(token);
        String nickname = jwtUtil.extractNickname(token);

        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(payload.getRoomId())
                .sender(userId)
                .nickname(nickname)
                .message(payload.getMessage())
                .submitted(payload.isSubmitted())
                .timestamp(LocalDateTime.now())
                .build();

        String teamId = getTeamId(session);

        if (payload.isSubmitted()) {
            messageRepository.save(chatMessage);
            String opponentTeam = getOpponentTeamId(teamId);
            sendToTeam(opponentTeam, chatMessage);
        } else {
            sendToTeam(teamId, chatMessage);
        }
    }

    private void sendToTeam(String teamId, ChatMessage msg) throws IOException {
        Set<WebSocketSession> sessions = sessionManager.getTeamSessions(teamId);
        String json = objectMapper.writeValueAsString(msg);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }

    private String getTeamId(@NonNull WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) return null;

        return UriComponentsBuilder.fromUriString("?" + uri.getQuery())
                .build()
                .getQueryParams()
                .getFirst("teamId");
    }

    private String extractToken(@NonNull WebSocketSession session) {
        String bearer = session.getHandshakeHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return bearer != null ? bearer.replace("Bearer ", "") : "";
    }

    private String getOpponentTeamId(String teamId) {
        if (teamId == null) return "";
        return teamId.endsWith("A") ? teamId.replace("A", "B") : teamId.replace("B", "A");
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String teamId = getTeamId(session);
        if (teamId != null) {
            sessionManager.unregister(teamId, session);
        }
    }
}
