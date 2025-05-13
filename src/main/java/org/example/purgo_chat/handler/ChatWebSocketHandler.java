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
import java.util.List;
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
                handleLeaveMessage(chatMessage);
                break;
        }
    }

    private void handleEnterMessage(WebSocketSession session, ChatMessageDto chatMessage) throws IOException {
        String senderName = chatMessage.getSender();
        ChatRoom chatRoom = chatService.getFixedChatRoom();

        try {
            // 사용자 입장 처리 (예외 발생 가능)
            chatService.handleUserEnter(senderName);
            userSessions.put(senderName, session);

            // ✅ 참여자 목록 전송
            List<String> participants = chatService.getCurrentParticipants(chatRoom);
            ChatMessageDto participantMessage = ChatMessageDto.builder()
                    .type(ChatMessageDto.MessageType.PARTICIPANTS)
                    .participants(participants)
                    .build();
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(participantMessage)));

            // ✅ 입장 메시지 브로드캐스트
            chatMessage.setRoomId(chatRoom.getId().toString());
            chatMessage.setTime(getCurrentTime());
            chatMessage.setContent(senderName + "님이 입장했습니다.");
            broadcastMessage(chatMessage);

        } catch (IllegalStateException e) {
            // ✅ ERROR 메시지 전송
            ChatMessageDto errorMessage = ChatMessageDto.builder()
                    .type(ChatMessageDto.MessageType.ERROR)
                    .content(e.getMessage())
                    .build();
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
            log.warn("입장 거부: {} (사유: {})", senderName, e.getMessage());
            session.close();
        }
    }

    private void handleTalkMessage(ChatMessageDto chatMessage) throws IOException {
        String senderName = chatMessage.getSender();
        String content = chatMessage.getContent();

        ChatRoom chatRoom = chatService.getFixedChatRoom();

        // 욕설 필터링
        FilterResponse filterResponse = badwordFilterService.filterMessage(content, chatRoom, senderName);
        String filteredContent = filterResponse.getDisplayText();

        // 메시지 저장
        chatService.saveMessage(chatRoom, senderName, filteredContent);

        // 메시지 설정
        chatMessage.setContent(filteredContent);
        chatMessage.setTime(getCurrentTime());
        chatMessage.setRoomId(chatRoom.getId().toString());
        chatMessage.setBadWordCount(chatRoom.getBadwordCount());

        broadcastMessage(chatMessage);
    }

    private void handleLeaveMessage(ChatMessageDto chatMessage) throws IOException {
        String senderName = chatMessage.getSender();

        // 세션 제거 및 종료
        WebSocketSession session = userSessions.remove(senderName);
        if (session != null && session.isOpen()) {
            session.close();
        }

        ChatRoom chatRoom = chatService.getFixedChatRoom();
        chatService.handleUserLeave(chatRoom, senderName);

        // 퇴장 메시지 설정
        chatMessage.setTime(getCurrentTime());
        chatMessage.setContent(senderName + "님이 퇴장했습니다.");
        broadcastMessage(chatMessage);
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
                ChatMessageDto leaveMessage = new ChatMessageDto();
                leaveMessage.setType(ChatMessageDto.MessageType.LEAVE);
                leaveMessage.setSender(disconnectedUser);
                handleLeaveMessage(leaveMessage);
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
