package org.example.purgo_chat.repository;

import org.example.purgo_chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChatRoomIdOrderByCreatedAtAsc(Integer chatRoomId);
}