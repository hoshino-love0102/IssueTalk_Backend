package com.issuetalk.chat.repository;

import com.issuetalk.chat.domain.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// ChatMessage 엔티티를 위한 MongoDB 리포지토리 인터페이스
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByRoomId(String roomId); // 특정 roomId에 해당하는 채팅 메시지 목록 조회
}