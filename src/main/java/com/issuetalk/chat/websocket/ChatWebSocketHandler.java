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
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageRepository messageRepository;
    private final JwtUtil jwtUtil;
    private final ChatSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        // teamId 확인
        String teamId = getTeamId(session);

        String token = extractToken(session);
        if (!StringUtils.hasText(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing token"));
            return;
        }

        // 최소한 userId 추출 가능해야 함
        try {
            String userId = jwtUtil.extractUserId(token);
            if (!StringUtils.hasText(userId)) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
                return;
            }
        } catch (Exception e) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            return;
        }

        if (teamId != null) {
            sessionManager.register(teamId, session);
        }
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
        ChatPayload payload = objectMapper.readValue(message.getPayload(), ChatPayload.class);

        String token = extractToken(session);
        if (!StringUtils.hasText(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing token"));
            return;
        }

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
            // 제출 메시지는 DB 저장 후 상대 팀에게만 전송
            messageRepository.save(chatMessage);
            String opponentTeam = getOpponentTeamId(teamId);
            sendToTeam(opponentTeam, chatMessage);
        } else {
            // 입력 중/일반 메시지는 같은 팀에게만 브로드캐스트
            sendToTeam(teamId, chatMessage);
        }
    }

    private void sendToTeam(String teamId, ChatMessage msg) throws IOException {
        if (!StringUtils.hasText(teamId)) return;

        Set<WebSocketSession> sessions = sessionManager.getTeamSessions(teamId);
        String json = objectMapper.writeValueAsString(msg);

        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    private String getTeamId(@NonNull WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;

        MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
        return params.getFirst("teamId"); // 예: room123-A, room123-B
    }

    // 쿼리스트링 token 우선 -> (가능한 경우) Authorization 헤더 fallback
    private String extractToken(@NonNull WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null) {
            String q = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("token");
            if (StringUtils.hasText(q)) return q;
        }
        String bearer = session.getHandshakeHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return "";
    }

    private String getOpponentTeamId(String teamId) {
        if (!StringUtils.hasText(teamId)) return "";
        return teamId.endsWith("A") ? teamId.substring(0, teamId.length() - 1) + "B"
                : teamId.substring(0, teamId.length() - 1) + "A";
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String teamId = getTeamId(session);
        if (teamId != null) {
            sessionManager.unregister(teamId, session);
        }
    }
}
