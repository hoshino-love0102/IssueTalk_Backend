package com.issuetalk.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetalk.chat.domain.ChatMessage;
import com.issuetalk.chat.dto.ChatPayload;
import com.issuetalk.chat.repository.ChatMessageRepository;
import com.issuetalk.chat.jwt.JwtUtil;
import com.issuetalk.chat.service.ChatSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@Component // WebSocket 핸들러로 등록
@RequiredArgsConstructor // 생성자 주입 자동 생성
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageRepository messageRepository; // 메시지 저장소
    private final JwtUtil jwtUtil; // JWT 유틸
    private final ChatSessionManager sessionManager; // 세션 관리자
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파서

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 연결되면 세션을 팀에 등록
        String teamId = getTeamId(session);
        if (teamId != null) {
            sessionManager.register(teamId, session);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // 메시지 수신 시 처리
        ChatPayload payload = objectMapper.readValue(message.getPayload(), ChatPayload.class);

        String token = extractToken(session); // JWT 추출
        String username = jwtUtil.extractUserId(token); // userId 추출
        String nickname = jwtUtil.extractNickname(token); // nickname 추출

        // 메시지 객체 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(payload.getRoomId())
                .sender(username)
                .nickname(nickname)
                .message(payload.getMessage())
                .submitted(payload.isSubmitted())
                .timestamp(LocalDateTime.now())
                .build();

        String teamId = getTeamId(session); // 현재 팀 ID 추출

        if (payload.isSubmitted()) {
            // 제출된 메시지는 DB에 저장 후 상대 팀에게 전송
            messageRepository.save(chatMessage);
            String opponentTeam = getOpponentTeamId(teamId);
            sendToTeam(opponentTeam, chatMessage);
        } else {
            // 작성 중 메시지는 같은 팀에게만 전송
            sendToTeam(teamId, chatMessage);
        }
    }

    private void sendToTeam(String teamId, ChatMessage msg) throws IOException {
        // 특정 팀의 세션에 메시지 전송
        Set<WebSocketSession> sessions = sessionManager.getTeamSessions(teamId);
        String json = objectMapper.writeValueAsString(msg);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }

    private String getTeamId(WebSocketSession session) {
        // WebSocket 연결 URL에서 teamId 쿼리 추출
        String query = session.getUri().getQuery();
        return UriComponentsBuilder.fromUriString("?" + query)
                .build()
                .getQueryParams()
                .getFirst("teamId"); // 클라이언트에서 반드시 teamId도 같이 보내야 함
    }

    private String extractToken(WebSocketSession session) {
        // WebSocket 헤더에서 JWT 추출
        String bearer = session.getHandshakeHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return bearer != null ? bearer.replace("Bearer ", "") : "";
    }

    private String getOpponentTeamId(String teamId) {
        // 상대 팀 ID 반환 (예: A <-> B)
        if (teamId == null) return "";
        return teamId.endsWith("A") ? teamId.replace("A", "B") : teamId.replace("B", "A");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 연결 종료 시 세션 해제
        String teamId = getTeamId(session);
        if (teamId != null) {
            sessionManager.unregister(teamId, session);
        }
    }
}
