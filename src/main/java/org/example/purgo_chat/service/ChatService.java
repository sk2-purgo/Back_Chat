package org.example.purgo_chat.service;

import lombok.RequiredArgsConstructor;
import org.example.purgo_chat.entity.ChatRoom;
import org.example.purgo_chat.entity.Message;
import org.example.purgo_chat.repository.ChatRoomRepository;
import org.example.purgo_chat.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    // ✅ 1번방 가져오기 (없으면 새로 만들기)
    public ChatRoom getFixedChatRoom() {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(1);
        if (chatRoomOpt.isPresent()) {
            return chatRoomOpt.get();
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .user1Name("system")
                .user2Name("system")
                .badwordCount(0)
                .leaveCount(0)
                .build();
        return chatRoomRepository.save(chatRoom);
    }

    // ✅ 채팅방에 메시지 저장
    public void saveMessage(ChatRoom chatRoom, String senderName, String content) {
        Message message = Message.builder()
                .chatRoom(chatRoom)
                .senderName(senderName)
                .receiverName("all")
                .content(content)
                .build();

        messageRepository.save(message);
    }

    // ✅ 욕설 사용 시 badword 카운트 증가
    public void incrementBadwordCount(ChatRoom chatRoom) {
        chatRoom.setBadwordCount(chatRoom.getBadwordCount() + 1);
        chatRoomRepository.save(chatRoom);
    }

    // ✅ 사용자가 퇴장할 때 leaveCount 증가
    public void incrementLeaveCount(ChatRoom chatRoom) {
        chatRoom.setLeaveCount(chatRoom.getLeaveCount() + 1);
        chatRoomRepository.save(chatRoom);
    }

    // ✅ leave_count가 2가 되면 채팅방과 메시지 삭제
    public void clearChatRoom(ChatRoom chatRoom) {
        messageRepository.deleteAllByChatRoomId(chatRoom.getId());
        chatRoomRepository.delete(chatRoom);
    }

    // ✅ 채팅 기록 조회
    public List<Message> getChatHistory(Integer chatRoomId) {
        return messageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
    }
}
