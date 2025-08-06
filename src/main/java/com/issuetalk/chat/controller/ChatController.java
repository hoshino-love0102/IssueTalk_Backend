package com.issuetalk.chat.controller;

import com.issuetalk.chat.domain.ChatMessage;
import com.issuetalk.chat.dto.ChatRoomDto;
import com.issuetalk.chat.service.ChatService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/room")
    public ResponseEntity<String> createRoom(@RequestParam String topicId,
                                             @RequestParam(defaultValue = "3") int maxTeamSize) {
        String roomId = chatService.createRoom(topicId, maxTeamSize);
        return ResponseEntity.ok(roomId);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getMessagesByRoom(roomId));
    }

    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<?> joinTeam(@PathVariable String roomId,
                                      @RequestParam String team,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            String joinedTeam = chatService.joinTeam(roomId, token, team);
            return ResponseEntity.ok(Map.of("success", true, "team", joinedTeam));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDto>> getUserRooms(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(chatService.getRoomsByUser(token));
    }

    // 전체 채팅방 목록 조회
    @GetMapping("/room/all")
    public ResponseEntity<List<ChatRoomDto>> getAllRooms() {
        return ResponseEntity.ok(chatService.getAllRooms());
    }

    // 특정 roomId의 topic 조회
    @GetMapping("/room/{roomId}/topic")
    public ResponseEntity<?> getTopicByRoomId(@PathVariable String roomId) {
        String topic = chatService.getTopicByRoomId(roomId);
        if (topic == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "해당 roomId의 채팅방이 존재하지 않습니다."));
        }
        return ResponseEntity.ok(Map.of("roomId", roomId, "topicId", topic));
    }
}
