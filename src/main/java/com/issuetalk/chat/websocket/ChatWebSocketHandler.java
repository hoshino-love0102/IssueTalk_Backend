package com.issuetalk.chat.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetalk.chat.ChatMessage;
import com.issuetalk.chat.ChatMessageRepository;
import com.issuetalk.chat.ChatPayload;
import com.issuetalk.chat.ChatSessionManager;
import com.issuetalk.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageRepository messageRepository;
    private final JwtProvider jwtProvider;
    private final ChatSessionManager sessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // WebSocket 연결 시 호출됨
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String roomId = getRoomId(session);
        String teamId = getTeamId(roomId);
        sessionManager.register(teamId, session);
    }

    // 메시지 수신 시 호출됨
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        ChatPayload payload = objectMapper.readValue(message.getPayload(), ChatPayload.class);

        // 사용자 정보 파싱
        String token = extractToken(session);
        String username = jwtProvider.getUsernameFromToken(token);
        String nickname = jwtProvider.getNicknameFromToken(token);

        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(payload.getRoomId())
                .sender(username)
                .nickname(nickname)
                .message(payload.getMessage())
                .submitted(payload.isSubmitted())
                .timestamp(LocalDateTime.now())
                .build();

        String currentTeam = getTeamId(payload.getRoomId());

        if (payload.isSubmitted()) {
            // DB 저장 후 상대 팀으로 전송
            messageRepository.save(chatMessage);
            String opponentTeam = getOpponentTeamId(currentTeam);
            sendToTeam(opponentTeam, chatMessage);
        } else {
            // 수정 중 → 같은 팀 실시간 전송
            sendToTeam(currentTeam, chatMessage);
        }
    }

    // 팀 세션에 메시지 전송
    private void sendToTeam(String teamId, ChatMessage msg) throws IOException {
        Set<WebSocketSession> sessions = sessionManager.getTeamSessions(teamId);
        String json = objectMapper.writeValueAsString(msg);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }

    // 세션 URL에서 roomId 추출
    private String getRoomId(WebSocketSession session) {
        String query = session.getUri().getQuery(); // roomId=123A
        return UriComponentsBuilder.fromUriString("?" + query).build().getQueryParams().getFirst("roomId");
    }

    // WebSocket 헤더에서 토큰 추출
    private String extractToken(WebSocketSession session) {
        String bearer = session.getHandshakeHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return bearer != null ? bearer.replace("Bearer ", "") : "";
    }

    // roomId 끝이 A or B 인 경우 팀 ID 반환
    private String getTeamId(String roomId) {
        return roomId.substring(roomId.length() - 1); // 마지막 글자 A/B
    }

    private String getOpponentTeamId(String teamId) {
        return teamId.equals("A") ? "B" : "A";
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = getRoomId(session);
        String teamId = getTeamId(roomId);
        sessionManager.unregister(teamId, session);
    }
}
