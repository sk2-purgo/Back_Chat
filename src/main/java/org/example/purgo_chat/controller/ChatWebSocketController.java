package org.example.purgo_chat.controller;


import lombok.RequiredArgsConstructor;
import org.example.purgo_chat.entity.Message;
import org.example.purgo_chat.service.ChatRoomService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatRoomService chatRoomService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public Message sendMessage(Message message) {
        return chatRoomService.processMessage(
                message.getChatRoom().getId().toString(),
                message.getSenderName(),
                message.getContent()
        );
    }
}
