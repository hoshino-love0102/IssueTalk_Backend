package com.issuetalk.chat.service;

import com.issuetalk.chat.domain.ChatMessage;
import com.issuetalk.chat.domain.ChatRoom;
import com.issuetalk.chat.dto.ChatRoomDto;
import com.issuetalk.chat.jwt.JwtUtil;
import com.issuetalk.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionManager sessionManager;
    private final JwtUtil jwtUtil;

    // 채팅 메시지 저장
    public void saveMessage(ChatMessage message) {
        chatMessageRepository.save(message);
    }

    // 특정 채팅방의 메시지 목록 조회
    public List<ChatMessage> getMessagesByRoom(String roomId) {
        return chatMessageRepository.findByRoomId(roomId);
    }

    // 채팅방 생성 (token 사용 제거됨)
    public String createRoom(String topicId, int maxTeamSize) {
        String roomId = UUID.randomUUID().toString().substring(0, 8);

        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .topicId(topicId)
                .maxTeamSize(maxTeamSize)
                .teamAUserIds(new ArrayList<>())
                .teamBUserIds(new ArrayList<>())
                .build();

        sessionManager.saveRoom(topicId, room);
        return roomId;
    }

    // 팀 참가
    public String joinTeam(String roomId, String token, String team) {
        String userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        ChatRoom room = sessionManager.getRoomByRoomId(roomId);

        if (room == null) throw new IllegalArgumentException("존재하지 않는 방입니다.");

        int max = room.getMaxTeamSize();

        if (team.equalsIgnoreCase("A")) {
            List<String> teamA = room.getTeamAUserIds();
            if (teamA.contains(userId)) return "A";
            if (teamA.size() >= max) throw new IllegalStateException("팀 A는 정원이 가득 찼습니다.");
            teamA.add(userId);
        } else if (team.equalsIgnoreCase("B")) {
            List<String> teamB = room.getTeamBUserIds();
            if (teamB.contains(userId)) return "B";
            if (teamB.size() >= max) throw new IllegalStateException("팀 B는 정원이 가득 찼습니다.");
            teamB.add(userId);
        } else {
            throw new IllegalArgumentException("팀은 A 또는 B만 선택 가능합니다.");
        }

        return team.toUpperCase();
    }

    // 내가 참여한 채팅방 목록 조회
    public List<ChatRoomDto> getRoomsByUser(String token) {
        String userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        List<ChatRoomDto> result = new ArrayList<>();

        for (Map.Entry<String, ChatRoom> entry : sessionManager.getAllRooms().entrySet()) {
            String topicId = entry.getKey();
            ChatRoom room = entry.getValue();

            boolean isInRoom = room.getTeamAUserIds().contains(userId) || room.getTeamBUserIds().contains(userId);
            if (isInRoom) {
                result.add(toDto(room, topicId));
            }
        }

        return result;
    }

    // 전체 채팅방 목록 조회
    public List<ChatRoomDto> getAllRooms() {
        List<ChatRoomDto> result = new ArrayList<>();

        for (Map.Entry<String, ChatRoom> entry : sessionManager.getAllRooms().entrySet()) {
            String topicId = entry.getKey();
            ChatRoom room = entry.getValue();
            result.add(toDto(room, topicId));
        }

        return result;
    }

    // roomId에서 topicId 조회
    public String getTopicByRoomId(String roomId) {
        ChatRoom room = sessionManager.getRoomByRoomId(roomId);
        return room != null ? room.getTopicId() : null;
    }

    // 중복 제거용 헬퍼 메서드
    private ChatRoomDto toDto(ChatRoom room, String topicId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(room.getRoomId());
        ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);

        return new ChatRoomDto(
                room.getRoomId(),
                topicId,
                lastMessage != null ? lastMessage.getMessage() : "",
                lastMessage != null ? lastMessage.getTimestamp() : null
        );
    }
}
