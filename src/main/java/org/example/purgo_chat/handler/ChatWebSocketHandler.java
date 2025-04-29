package org.example.purgo_chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.purgo_chat.dto.ChatMessageDto;
import org.example.purgo_chat.dto.FilterResponse;
import org.example.purgo_chat.entity.ChatRoom;
import org.example.purgo_chat.service.BadwordFilterService;
import org.example.purgo_chat.service.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatService chatService;
    private final BadwordFilterService badwordFilterService;

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("웹소켓 연결 성립: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("수신 메시지: {}", payload);

        ChatMessageDto chatMessage = objectMapper.readValue(payload, ChatMessageDto.class);

        switch (chatMessage.getType()) {
            case ENTER:
                handleEnterMessage(session, chatMessage);
                break;
            case TALK:
                handleTalkMessage(chatMessage);
                break;
            case LEAVE:
                handleLeaveMessage(chatMessage.getSender());
                break;
        }
    }

    private void handleEnterMessage(WebSocketSession session, ChatMessageDto chatMessage) throws IOException {
        String senderName = chatMessage.getSender();

        ChatRoom chatRoom = chatService.getFixedChatRoom();
        userSessions.put(senderName, session);

        chatMessage.setRoomId(chatRoom.getId().toString());
        chatMessage.setTime(getCurrentTime());
        chatMessage.setContent(senderName + "님이 입장했습니다.");

        broadcastMessage(chatMessage);
    }

    private void handleTalkMessage(ChatMessageDto chatMessage) throws IOException {
        String senderName = chatMessage.getSender();
        String content = chatMessage.getContent();

        ChatRoom chatRoom = chatService.getFixedChatRoom();

        FilterResponse filterResponse = badwordFilterService.filterMessage(content, chatRoom, senderName);
        String filteredContent = filterResponse.getRewrittenText();

        chatService.saveMessage(chatRoom, senderName, filteredContent);

        chatMessage.setContent(filteredContent);
        chatMessage.setTime(getCurrentTime());

        broadcastMessage(chatMessage);
    }

    private void handleLeaveMessage(String senderName) throws IOException {
        WebSocketSession session = userSessions.remove(senderName);
        if (session != null) {
            session.close();
        }

        ChatRoom chatRoom = chatService.getFixedChatRoom();
        chatService.incrementLeaveCount(chatRoom);

        ChatMessageDto leaveMessage = new ChatMessageDto();
        leaveMessage.setType(ChatMessageDto.MessageType.LEAVE);
        leaveMessage.setSender(senderName);
        leaveMessage.setTime(getCurrentTime());
        leaveMessage.setContent(senderName + "님이 퇴장했습니다.");

        broadcastMessage(leaveMessage);

        if (chatRoom.getLeaveCount() >= 2) {
            chatService.clearChatRoom(chatRoom);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("웹소켓 연결 종료: {}, 상태: {}", session.getId(), status);

        String disconnectedUser = null;
        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                disconnectedUser = entry.getKey();
                break;
            }
        }

        if (disconnectedUser != null) {
            try {
                handleLeaveMessage(disconnectedUser);
            } catch (IOException e) {
                log.error("afterConnectionClosed 오류", e);
            }
        }
    }

    private void broadcastMessage(ChatMessageDto chatMessage) throws IOException {
        String messageJson = objectMapper.writeValueAsString(chatMessage);
        TextMessage textMessage = new TextMessage(messageJson);

        for (WebSocketSession session : userSessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(textMessage);
            }
        }
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
