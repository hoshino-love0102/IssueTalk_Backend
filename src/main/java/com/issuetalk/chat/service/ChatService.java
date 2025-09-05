package com.issuetalk.chat.service;

import com.issuetalk.chat.domain.ChatMessage;
import com.issuetalk.chat.domain.ChatRoom;
import com.issuetalk.chat.dto.ChatRoomDto;
import com.issuetalk.chat.jwt.JwtUtil;
import com.issuetalk.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionManager sessionManager;
    private final JwtUtil jwtUtil;

    // 메시지 저장
    public void saveMessage(ChatMessage message) {
        chatMessageRepository.save(message);
    }

    // 메시지 조회
    public List<ChatMessage> getMessagesByRoom(String roomId) {
        log.info("[GET_MESSAGES] roomId={}", roomId);
        return chatMessageRepository.findByRoomId(roomId);
    }

    // 방 생성 (로그인 사용자 기반)
    public String createRoom(String bearerToken, String topicId, int maxTeamSize) {
        String token = bearerToken.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);
        if (userId == null) throw new IllegalArgumentException("유효하지 않은 토큰");

        String roomId = UUID.randomUUID().toString().substring(0, 8);

        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .topicId(topicId)
                .maxTeamSize(maxTeamSize)
                .teamAUserIds(new ArrayList<>(List.of(userId))) // 생성자는 팀 A에 자동 참가
                .teamBUserIds(new ArrayList<>())
                .build();

        sessionManager.saveRoom(topicId, room);
        log.info("[CREATE_ROOM] userId={} created roomId={}, topicId={}", userId, roomId, topicId);

        return roomId;
    }

    // 팀 참가
    public String joinTeam(String roomId, String bearerToken, String team) {
        String token = bearerToken.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);

        ChatRoom room = sessionManager.getRoomByRoomId(roomId);
        if (room == null) throw new IllegalArgumentException("존재하지 않는 방입니다.");

        int max = room.getMaxTeamSize();

        if ("A".equalsIgnoreCase(team)) {
            if (room.getTeamAUserIds().contains(userId)) return "A";
            if (room.getTeamAUserIds().size() >= max) throw new IllegalStateException("팀 A 정원 초과");
            room.getTeamAUserIds().add(userId);
        } else if ("B".equalsIgnoreCase(team)) {
            if (room.getTeamBUserIds().contains(userId)) return "B";
            if (room.getTeamBUserIds().size() >= max) throw new IllegalStateException("팀 B 정원 초과");
            room.getTeamBUserIds().add(userId);
        } else {
            throw new IllegalArgumentException("팀은 A 또는 B만 가능합니다.");
        }

        log.info("[JOIN_TEAM] userId={} joined team={} in roomId={}", userId, team.toUpperCase(), roomId);
        return team.toUpperCase();
    }

    // 내 방 목록
    public List<ChatRoomDto> getRoomsByUser(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);

        List<ChatRoomDto> result = new ArrayList<>();
        for (ChatRoom room : sessionManager.getAllRooms().values()) {
            boolean inRoom = room.getTeamAUserIds().contains(userId) || room.getTeamBUserIds().contains(userId);
            if (inRoom) result.add(toDto(room));
        }
        return result;
    }

    // 전체 방 목록
    public List<ChatRoomDto> getAllRooms() {
        List<ChatRoomDto> result = new ArrayList<>();
        for (ChatRoom room : sessionManager.getAllRooms().values()) {
            result.add(toDto(room));
        }
        return result;
    }

    // roomId로 topicId 조회
    public String getTopicByRoomId(String roomId) {
        ChatRoom room = sessionManager.getRoomByRoomId(roomId);
        return room != null ? room.getTopicId() : null;
    }

    // DTO 변환
    private ChatRoomDto toDto(ChatRoom room) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(room.getRoomId());
        ChatMessage last = messages.isEmpty() ? null : messages.get(messages.size() - 1);

        return new ChatRoomDto(
                room.getRoomId(),
                room.getTopicId(),
                last != null ? last.getMessage() : "",
                last != null ? last.getTimestamp() : null
        );
    }
}
