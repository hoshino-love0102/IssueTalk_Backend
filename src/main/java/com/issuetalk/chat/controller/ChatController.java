package com.issuetalk.chat.controller;

import com.issuetalk.chat.domain.ChatMessage;
import com.issuetalk.chat.service.ChatService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService; // 채팅 서비스 의존성 주입

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/room") // 채팅방 생성 요청 처리
    public ResponseEntity<String> createRoom(@RequestParam String topicId,
                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        String roomId = chatService.createRoom(topicId, token); // 채팅방 생성
        return ResponseEntity.ok(roomId); // 생성된 roomId 반환
    }

    @GetMapping("/room/{roomId}") // 채팅 메시지 조회 요청 처리
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable String roomId) {
        List<ChatMessage> messages = chatService.getMessagesByRoom(roomId); // 메시지 목록 조회
        return ResponseEntity.ok(messages); // 메시지 리스트 반환
    }
}
