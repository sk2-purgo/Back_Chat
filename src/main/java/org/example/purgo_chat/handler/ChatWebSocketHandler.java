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
                handleLeaveMessage(chatMessage);
                break;
        }
    }

    private void handleEnterMessage(WebSocketSession session, ChatMessageDto chatMessage) throws IOException {
        String senderName = chatMessage.getSender();

        // 고정 채팅방 가져오기
        ChatRoom chatRoom = chatService.getFixedChatRoom();

        // 사용자 입장 처리 (닉네임 저장)
        chatService.handleUserEnter(senderName);

        // 세션 저장
        userSessions.put(senderName, session);

        // 메시지 설정
        chatMessage.setRoomId(chatRoom.getId().toString());
        chatMessage.setTime(getCurrentTime());
        chatMessage.setContent(senderName + "님이 입장했습니다.");

        // 메시지 브로드캐스트
        broadcastMessage(chatMessage);
    }

    private void handleTalkMessage(ChatMessageDto chatMessage) throws IOException {
        String senderName = chatMessage.getSender();
        String content = chatMessage.getContent();

        ChatRoom chatRoom = chatService.getFixedChatRoom();

        // 비속어 필터링
        FilterResponse filterResponse = badwordFilterService.filterMessage(content, chatRoom, senderName);
        String filteredContent = filterResponse.getDisplayText();   // ✅ 표시용 문장

        // 메시지 저장 - 송신자와 수신자 정보가 적절히 설정됨
        chatService.saveMessage(chatRoom, senderName, filteredContent);

        // 메시지 설정
        chatMessage.setContent(filteredContent);
        chatMessage.setTime(getCurrentTime());

        // 채팅방 ID 설정 (고정 채팅방이므로 항상 동일)
        chatMessage.setRoomId(chatRoom.getId().toString());

        // 메시지 브로드캐스트
        broadcastMessage(chatMessage);
    }

    private void handleLeaveMessage(ChatMessageDto chatMessage) throws IOException {
        String senderName = chatMessage.getSender();

        // 세션 제거 및 닫기
        WebSocketSession session = userSessions.remove(senderName);
        if (session != null && session.isOpen()) {
            session.close();
        }

        // 채팅방 가져오기
        ChatRoom chatRoom = chatService.getFixedChatRoom();

        // 사용자 퇴장 처리
        chatService.handleUserLeave(chatRoom, senderName);

        // 메시지 설정
        chatMessage.setTime(getCurrentTime());
        chatMessage.setContent(senderName + "님이 퇴장했습니다.");

        // 메시지 브로드캐스트
        broadcastMessage(chatMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("웹소켓 연결 종료: {}, 상태: {}", session.getId(), status);

        // 연결이 끊긴 사용자 찾기
        String disconnectedUser = null;
        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                disconnectedUser = entry.getKey();
                break;
            }
        }

        // 퇴장 처리
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

        // 모든 연결된 세션에 메시지 전송
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