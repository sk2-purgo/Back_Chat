package org.example.purgo_chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.purgo_chat.entity.ChatRoom;
import org.example.purgo_chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 고정 채팅방의 badword_count 조회 API
    @GetMapping("/count")
    public ResponseEntity<Integer> getBadwordCount() {
        ChatRoom chatRoom = chatService.getFixedChatRoom();
        return ResponseEntity.ok(chatRoom.getBadwordCount());
    }

}