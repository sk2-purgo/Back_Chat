package org.example.purgo_chat.repository;

import org.example.purgo_chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    @Modifying
    @Query("UPDATE ChatRoom c SET c.badwordCount = c.badwordCount + 1 WHERE c.id = :id")
    void incrementBadwordCount(@Param("id") int id);
}