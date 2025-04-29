package org.example.purgo_chat.service;

import lombok.RequiredArgsConstructor;
import org.example.purgo_chat.entity.ChatRoom;
import org.example.purgo_chat.entity.Message;
import org.example.purgo_chat.repository.ChatRoomRepository;
import org.example.purgo_chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${proxy.server.url}")
    private String gatewayUrl;

    public ChatRoom enterRoom(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해야 합니다.");
        }

        ChatRoom room = chatRoomRepository.findById(1).orElse(null);

        if (room == null) {
            room = new ChatRoom();
            room.setUser1Name(nickname);
            return chatRoomRepository.save(room);
        } else if (room.getUser2Name() == null) {
            room.setUser2Name(nickname);
            return chatRoomRepository.save(room);
        } else {
            throw new IllegalStateException("채팅방이 가득 찼습니다.");
        }
    }


    public Message processMessage(String roomId, String sender, String content) {
        ChatRoom room = chatRoomRepository.findById(Integer.parseInt(roomId))
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        String filteredContent = getFilteredText(content, room);

        Message message = new Message();
        message.setChatRoom(room);
        message.setSenderName(sender);
        message.setContent(filteredContent);
        message.setCreatedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    public List<Message> getMessages(int roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 없습니다."));
        return messageRepository.findByChatRoom(room);
    }

    private String getFilteredText(String text, ChatRoom room) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            // 여기가 중요!
            ResponseEntity<Map> response = restTemplate.postForEntity(gatewayUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();

                Object decision = result.get("final_decision");
                Boolean isAbusive = decision != null && decision.toString().equals("1");

                Map<String, Object> resultInner = (Map<String, Object>) result.get("result");
                String rewritten = resultInner != null ? (String) resultInner.get("rewritten_text") : text;

                if (Boolean.TRUE.equals(isAbusive)) {
                    room.setBadwordCount(room.getBadwordCount() + 1);
                    chatRoomRepository.save(room);
                }

                return rewritten;
            }
        } catch (Exception e) {
            System.out.println("❌ 욕설 분석 실패: " + e.getMessage());
        }
        return text;
    }
}
