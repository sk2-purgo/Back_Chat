package org.example.purgo_chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.purgo_chat.entity.ChatRoom;
import org.example.purgo_chat.entity.Message;
import org.example.purgo_chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 1번방 고정으로 가져오는 API
    @GetMapping("/room")
    public ResponseEntity<ChatRoom> getChatRoom() {
        ChatRoom chatRoom = chatService.getFixedChatRoom();
        return ResponseEntity.ok(chatRoom);
    }

    // 채팅 기록 가져오는 API
    @GetMapping("/history")
    public ResponseEntity<List<Message>> getChatHistory() {
        ChatRoom chatRoom = chatService.getFixedChatRoom();
        List<Message> messages = chatService.getChatHistory(chatRoom.getId());
        return ResponseEntity.ok(messages);
    }
}