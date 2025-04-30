package org.example.purgo_chat.service;

import lombok.RequiredArgsConstructor;
import org.example.purgo_chat.entity.ChatRoom;
import org.example.purgo_chat.entity.Message;
import org.example.purgo_chat.repository.ChatRoomRepository;
import org.example.purgo_chat.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    // 최근에 나간 사용자를 추적하기 위한 맵 (사용자 이름 -> 마지막으로 있었던 위치)
    private final Map<String, String> recentlyLeft = new HashMap<>();

    // 1번방 가져오기 (없으면 새로 만들기)
    public ChatRoom getFixedChatRoom() {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(1);
        if (chatRoomOpt.isPresent()) {
            return chatRoomOpt.get();
        }

        // 채팅방이 없으면 초기 상태로 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .user1Name(null)
                .user2Name(null)
                .badwordCount(0)
                .leaveCount(0)
                .build();
        return chatRoomRepository.save(chatRoom);
    }

    // 사용자 입장 처리 및 닉네임 설정
    public void handleUserEnter(String username) {
        ChatRoom chatRoom = getFixedChatRoom();

        // 이미 동일한 사용자가 채팅방에 있는지 확인
        if (username.equals(chatRoom.getUser1Name()) || username.equals(chatRoom.getUser2Name())) {
            // 이미 등록된 사용자이므로 아무 작업도 수행하지 않음
            return;
        }

        // 빈 자리가 있고 leaveCount가 0보다 크면 감소
        boolean isUser1Empty = chatRoom.getUser1Name() == null;
        boolean isUser2Empty = chatRoom.getUser2Name() == null;

        // 빈 자리가 있고 leaveCount가 0보다 크면, 같은 사용자 이름인지와 상관없이 감소
        if ((isUser1Empty || isUser2Empty) && chatRoom.getLeaveCount() > 0) {
            chatRoom.setLeaveCount(chatRoom.getLeaveCount() - 1);

            // 이 사용자가 최근에 나간 사용자라면 맵에서 제거
            recentlyLeft.remove(username);
        }

        // user1Name이 비어있으면 첫 번째 사용자
        if (isUser1Empty) {
            chatRoom.setUser1Name(username);
        }
        // user2Name이 비어있으면 두 번째 사용자
        else if (isUser2Empty) {
            chatRoom.setUser2Name(username);
        }
        // 채팅방이 이미 꽉 찬 경우 (이 부분은 클라이언트에서 추가 처리 필요)

        chatRoomRepository.save(chatRoom);
    }

    // 채팅방에 메시지 저장
    public void saveMessage(ChatRoom chatRoom, String senderName, String content) {
        // 수신자 닉네임 결정
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

    // 수신자 닉네임 결정하는 메서드
    private String getReceiverName(ChatRoom chatRoom, String senderName) {
        // user1이 보낸 메시지면 user2가 수신자
        if (senderName.equals(chatRoom.getUser1Name())) {
            return chatRoom.getUser2Name() != null ? chatRoom.getUser2Name() : "waiting";
        }
        // user2가 보낸 메시지면 user1이 수신자
        else if (senderName.equals(chatRoom.getUser2Name())) {
            return chatRoom.getUser1Name() != null ? chatRoom.getUser1Name() : "waiting";
        }
        // 예외 상황 처리 (둘 다 아닌 경우, 로그인되지 않은 사용자 등)
        return "waiting";
    }

    // 욕설 사용 시 badword 카운트 증가
    public void incrementBadwordCount(ChatRoom chatRoom) {
        chatRoom.setBadwordCount(chatRoom.getBadwordCount() + 1);
        chatRoomRepository.save(chatRoom);
    }

    // 사용자가 퇴장할 때 leaveCount 증가
    public void incrementLeaveCount(ChatRoom chatRoom) {
        chatRoom.setLeaveCount(chatRoom.getLeaveCount() + 1);
        chatRoomRepository.save(chatRoom);
    }

    // 사용자 퇴장시 처리
    public void handleUserLeave(ChatRoom chatRoom, String username) {
        // 퇴장한 사용자의 위치를 저장
        if (username.equals(chatRoom.getUser1Name())) {
            recentlyLeft.put(username, "user1");
            chatRoom.setUser1Name(null);
        } else if (username.equals(chatRoom.getUser2Name())) {
            recentlyLeft.put(username, "user2");
            chatRoom.setUser2Name(null);
        }

        // Leave 카운트 증가
        incrementLeaveCount(chatRoom);
        chatRoomRepository.save(chatRoom);

        // 두 사용자가 모두 나갔으면 채팅방 초기화
        if (chatRoom.getLeaveCount() == 2) {
            clearChatRoom(chatRoom);
            // 채팅방이 초기화되면 최근 나간 사용자 정보도 초기화
            recentlyLeft.clear();
        }
    }

    // leave_count가 2가 되면 채팅방과 메시지 초기화 (badwordCount 포함)
    public void clearChatRoom(ChatRoom chatRoom) {

        // 채팅방 초기화 (삭제 후 재생성이 아닌 정보 초기화)
        chatRoom.setUser1Name(null);
        chatRoom.setUser2Name(null);
        chatRoom.setBadwordCount(0); // badwordCount도 초기화
        chatRoom.setLeaveCount(0);   // leaveCount 초기화
        chatRoomRepository.save(chatRoom);
    }

    // 채팅 기록 조회
    public List<Message> getChatHistory(Integer chatRoomId) {
        return messageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
    }
}