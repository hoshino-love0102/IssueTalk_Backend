package com.issuetalk.chat.controller;

import com.issuetalk.chat.domain.ChatMessage;
import com.issuetalk.chat.dto.ChatRoomDto;
import com.issuetalk.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    public ChatController(ChatService chatService) { this.chatService = chatService; }

    // 방 생성: topicId가 없으면 자동 슬러그 생성
    @PostMapping("/room")
    public ResponseEntity<?> createRoom(
            @RequestParam(required = false) String topicId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "3") int maxTeamSize,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    ) {
        if (!hasText(topicId)) {
            if (hasText(category) && hasText(title)) {
                topicId = buildTopicId(category, title);
            }
        }
        if (!hasText(topicId)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "topicId 또는 category/title이 필요합니다."
            ));
        }
        String roomId = chatService.createRoom(token, topicId, maxTeamSize);
        return ResponseEntity.ok(Map.of("success", true, "roomId", roomId));
    }

    // 메시지 조회
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getMessagesByRoom(roomId));
    }

    // 팀 참가
    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<?> joinTeam(
            @PathVariable String roomId,
            @RequestParam String team,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            String joinedTeam = chatService.joinTeam(roomId, token, team);
            return ResponseEntity.ok(Map.of("success", true, "team", joinedTeam));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 내 방 목록
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDto>> getUserRooms(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(chatService.getRoomsByUser(token));
    }

    // 전체 방 목록
    @GetMapping("/room/all")
    public ResponseEntity<List<ChatRoomDto>> getAllRooms() {
        return ResponseEntity.ok(chatService.getAllRooms());
    }

    // topicId 조회
    @GetMapping("/room/{roomId}/topic")
    public ResponseEntity<?> getTopicByRoomId(@PathVariable String roomId) {
        String topic = chatService.getTopicByRoomId(roomId);
        if (topic == null) return ResponseEntity.badRequest().body(Map.of("message", "방이 존재하지 않음"));
        return ResponseEntity.ok(Map.of("roomId", roomId, "topicId", topic));
    }

    private static boolean hasText(String s) { return s != null && !s.trim().isEmpty(); }

    // 안전한 topicId 슬러그 생성
    private static String buildTopicId(String category, String title) {
        String base = (category == null ? "" : category.trim()) + "-" + (title == null ? "" : title.trim());
        String normalized = base
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase()
                .replaceAll("[^0-9a-z가-힣\\- ]", "")
                .replaceAll("\\s+", "-");
        if (normalized.length() > 64) normalized = normalized.substring(0, 64);
        return normalized.isEmpty() ? "topic" : normalized;
    }
}
