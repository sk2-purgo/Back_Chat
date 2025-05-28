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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (username.equals(chatRoom.getUser1Name()) ||
                username.equals(chatRoom.getUser2Name()) ||
                username.equals(chatRoom.getUser3Name()) ||
                username.equals(chatRoom.getUser4Name()) ||
                username.equals(chatRoom.getUser5Name()) ||
                username.equals(chatRoom.getUser6Name()) ||
                username.equals(chatRoom.getUser7Name()) ||
                username.equals(chatRoom.getUser8Name())){
            return;
        }

        // 빈 자리 여부 확인
        boolean isUser1Empty = chatRoom.getUser1Name() == null;
        boolean isUser2Empty = chatRoom.getUser2Name() == null;
        boolean isUser3Empty = chatRoom.getUser3Name() == null;
        boolean isUser4Empty = chatRoom.getUser4Name() == null;
        boolean isUser5Empty = chatRoom.getUser5Name() == null;
        boolean isUser6Empty = chatRoom.getUser6Name() == null;
        boolean isUser7Empty = chatRoom.getUser7Name() == null;
        boolean isUser8Empty = chatRoom.getUser8Name() == null;

        // 누군가 나갔고 빈 자리 있으면 leaveCount 감소
        if ((isUser1Empty || isUser2Empty || isUser3Empty || isUser4Empty || isUser5Empty || isUser6Empty || isUser7Empty || isUser8Empty)
                && chatRoom.getLeaveCount() > 0) {
            chatRoom.setLeaveCount(chatRoom.getLeaveCount() - 1);
            recentlyLeft.remove(username); // 이미 있던 코드 그대로 사용
        }

        if (isUser1Empty) {
            chatRoom.setUser1Name(username);
        } else if (isUser2Empty) {
            chatRoom.setUser2Name(username);
        } else if (isUser3Empty) {
            chatRoom.setUser3Name(username);
        } else if (isUser4Empty) {
            chatRoom.setUser4Name(username);
        } else if (isUser5Empty) {
            chatRoom.setUser5Name(username);
        } else if (isUser6Empty) {
            chatRoom.setUser6Name(username);
        } else if (isUser7Empty) {
            chatRoom.setUser7Name(username);
        } else if (isUser8Empty) {
            chatRoom.setUser8Name(username);
        } else {
            throw new IllegalStateException("채팅방이 이미 가득 찼습니다.");
        }

        chatRoomRepository.save(chatRoom);
    }

    private String getReceiverName(ChatRoom chatRoom, String senderName) {
        return getCurrentParticipants(chatRoom).stream()
                .filter(name -> !name.equals(senderName))
                .findFirst()
                .orElse("waiting");
    }

    // 메시지 저장
    public void saveMessage(ChatRoom chatRoom, String senderName, String content) {
        List<String> participants = getCurrentParticipants(chatRoom);

        // 채팅방 참여자 확인
        if (!participants.contains(senderName)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }

        // 수신자는 가장 먼저 발견되는 나 외의 사용자
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
        } else if (username.equals(chatRoom.getUser3Name())) {
            recentlyLeft.put(username, "user3");
            chatRoom.setUser3Name(null);
        } else if (username.equals(chatRoom.getUser4Name())) {
            recentlyLeft.put(username, "user4");
            chatRoom.setUser4Name(null);
        } else if (username.equals(chatRoom.getUser5Name())) {
            recentlyLeft.put(username, "user5");
            chatRoom.setUser5Name(null);
        } else if (username.equals(chatRoom.getUser6Name())) {
            recentlyLeft.put(username, "user6");
            chatRoom.setUser6Name(null);
        } else if (username.equals(chatRoom.getUser7Name())) {
            recentlyLeft.put(username, "user7");
            chatRoom.setUser7Name(null);
        } else if (username.equals(chatRoom.getUser8Name())) {
            recentlyLeft.put(username, "user8");
            chatRoom.setUser8Name(null);
        }


        chatRoom.setLeaveCount(chatRoom.getLeaveCount() + 1);
        chatRoomRepository.save(chatRoom);

        // 모두 나간 경우 초기화
        if (chatRoom.getLeaveCount() == 8) {
            clearChatRoom(chatRoom);
            recentlyLeft.clear();
        }
    }

    // 채팅방 초기화
    public void clearChatRoom(ChatRoom chatRoom) {
        chatRoom.setUser1Name(null);
        chatRoom.setUser2Name(null);
        chatRoom.setUser3Name(null);
        chatRoom.setUser4Name(null);
        chatRoom.setUser5Name(null);
        chatRoom.setUser6Name(null);
        chatRoom.setUser7Name(null);
        chatRoom.setUser8Name(null);
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
        return Stream.of(
                chatRoom.getUser1Name(),
                chatRoom.getUser2Name(),
                chatRoom.getUser3Name(),
                chatRoom.getUser4Name(),
                chatRoom.getUser5Name(),
                chatRoom.getUser6Name(),
                chatRoom.getUser7Name(),
                chatRoom.getUser8Name()
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
