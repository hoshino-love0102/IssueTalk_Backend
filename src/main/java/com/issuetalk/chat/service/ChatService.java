package com.issuetalk.chat.service;

import com.issuetalk.chat.domain.ChatMessage;
import com.issuetalk.chat.domain.ChatRoom;
import com.issuetalk.chat.repository.ChatMessageRepository;
import com.issuetalk.chat.service.ChatSessionManager;
import com.issuetalk.chat.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service // 비즈니스 로직을 담당하는 서비스 컴포넌트
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionManager sessionManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository,
                       ChatSessionManager sessionManager,
                       JwtUtil jwtUtil) {
        this.chatMessageRepository = chatMessageRepository;
        this.sessionManager = sessionManager;
        this.jwtUtil = jwtUtil;
    }

    // 채팅 메시지 저장
    public void saveMessage(ChatMessage message) {
        chatMessageRepository.save(message);
    }

    // 특정 채팅방의 메시지 목록 조회
    public List<ChatMessage> getMessagesByRoom(String roomId) {
        return chatMessageRepository.findByRoomId(roomId);
    }

    // 새로운 채팅방 생성 및 사용자 팀 배정
    public String createRoom(String topicId, String token) {
        String userId = jwtUtil.extractUserId(token.replace("Bearer ", "")); // 토큰에서 userId 추출
        String roomId = UUID.randomUUID().toString().substring(0, 8); // 8자리 랜덤 roomId 생성
        ChatRoom room = new ChatRoom();
        room.setRoomId(roomId);

        // 간단한 해시값 기준 팀 배정 로직
        if (userId.hashCode() % 2 == 0) {
            room.setTeamAUserIds(List.of(userId));
        } else {
            room.setTeamBUserIds(List.of(userId));
        }

        sessionManager.saveRoom(topicId, room); // 메모리 기반으로 방 저장
        return roomId;
    }
}
