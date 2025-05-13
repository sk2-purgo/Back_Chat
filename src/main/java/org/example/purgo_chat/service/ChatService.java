package org.example.purgo_chat.service;

import lombok.RequiredArgsConstructor;
import org.example.purgo_chat.entity.ChatRoom;
import org.example.purgo_chat.entity.Message;
import org.example.purgo_chat.repository.ChatRoomRepository;
import org.example.purgo_chat.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    // 최근에 나간 사용자 추적
    private final Map<String, String> recentlyLeft = new HashMap<>();

    // 고정된 채팅방(ID=1) 반환, 없으면 직접 생성
    public ChatRoom getFixedChatRoom() {
        return chatRoomRepository.findById(1).orElseGet(() -> {
            ChatRoom chatRoom = ChatRoom.builder()
                    .user1Name(null)
                    .user2Name(null)
                    .badwordCount(0)
                    .leaveCount(0)
                    .build();
            chatRoom.setId(1); // ID 수동 설정
            return chatRoomRepository.save(chatRoom);
        });
    }

    // 사용자 입장 처리
    public void handleUserEnter(String username) {
        ChatRoom chatRoom = getFixedChatRoom();

        // 이미 참여 중이면 무시
        if (username.equals(chatRoom.getUser1Name()) || username.equals(chatRoom.getUser2Name())) {
            return;
        }

        boolean isUser1Empty = chatRoom.getUser1Name() == null;
        boolean isUser2Empty = chatRoom.getUser2Name() == null;

        // 누군가 나갔고 빈 자리 있으면 leaveCount 감소
        if ((isUser1Empty || isUser2Empty) && chatRoom.getLeaveCount() > 0) {
            chatRoom.setLeaveCount(chatRoom.getLeaveCount() - 1);
            recentlyLeft.remove(username);
        }

        if (isUser1Empty) {
            chatRoom.setUser1Name(username);
        } else if (isUser2Empty) {
            chatRoom.setUser2Name(username);
        } else {
            // 🔒 세 번째 사용자 차단
            throw new IllegalStateException("채팅방이 이미 가득 찼습니다.");
        }

        chatRoomRepository.save(chatRoom);
    }

    // 메시지 저장
    public void saveMessage(ChatRoom chatRoom, String senderName, String content) {
        // 🔐 채팅방 참여자 확인
        if (!senderName.equals(chatRoom.getUser1Name()) && !senderName.equals(chatRoom.getUser2Name())) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }

        String receiverName = getReceiverName(chatRoom, senderName);

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .senderName(senderName)
                .receiverName(receiverName)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);
    }

    // 수신자 결정
    private String getReceiverName(ChatRoom chatRoom, String senderName) {
        if (senderName.equals(chatRoom.getUser1Name())) {
            return chatRoom.getUser2Name() != null ? chatRoom.getUser2Name() : "waiting";
        } else if (senderName.equals(chatRoom.getUser2Name())) {
            return chatRoom.getUser1Name() != null ? chatRoom.getUser1Name() : "waiting";
        }
        return "waiting";
    }

    // 욕설 카운트 증가
    @Transactional
    public void incrementBadwordCount(ChatRoom chatRoom) {
        chatRoom.setBadwordCount(chatRoom.getBadwordCount() + 1);
        chatRoomRepository.save(chatRoom);
    }

    // 사용자 퇴장 처리
    public void handleUserLeave(ChatRoom chatRoom, String username) {
        if (username.equals(chatRoom.getUser1Name())) {
            recentlyLeft.put(username, "user1");
            chatRoom.setUser1Name(null);
        } else if (username.equals(chatRoom.getUser2Name())) {
            recentlyLeft.put(username, "user2");
            chatRoom.setUser2Name(null);
        }

        chatRoom.setLeaveCount(chatRoom.getLeaveCount() + 1);
        chatRoomRepository.save(chatRoom);

        // 모두 나간 경우 초기화
        if (chatRoom.getLeaveCount() == 2) {
            clearChatRoom(chatRoom);
            recentlyLeft.clear();
        }
    }

    // 채팅방 초기화
    public void clearChatRoom(ChatRoom chatRoom) {
        chatRoom.setUser1Name(null);
        chatRoom.setUser2Name(null);
        chatRoom.setBadwordCount(0);
        chatRoom.setLeaveCount(0);
        chatRoomRepository.save(chatRoom);
    }

    // 채팅 기록 조회
    public List<Message> getChatHistory(Integer chatRoomId) {
        return messageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
    }

    // ✅ 현재 채팅방의 참여자 목록 반환
    public List<String> getCurrentParticipants(ChatRoom chatRoom) {
        List<String> participants = new ArrayList<>();
        if (chatRoom.getUser1Name() != null) participants.add(chatRoom.getUser1Name());
        if (chatRoom.getUser2Name() != null) participants.add(chatRoom.getUser2Name());
        return participants;
    }
}
