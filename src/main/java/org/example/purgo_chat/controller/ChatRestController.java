package org.example.purgo_chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.purgo_chat.entity.ChatRoom;
import org.example.purgo_chat.entity.Message;
import org.example.purgo_chat.service.ChatRoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatRestController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/enter")
    public ChatRoom enterRoom(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        return chatRoomService.enterRoom(nickname);
    }

    @GetMapping("/messages/{roomId}")
    public List<Message> getMessages(@PathVariable int roomId) {
        return chatRoomService.getMessages(roomId);
    }
}
