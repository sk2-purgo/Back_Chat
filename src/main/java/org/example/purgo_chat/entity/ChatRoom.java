package org.example.purgo_chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    private Integer id;

    @Column(name = "user1_name")
    private String user1Name;

    @Column(name = "user2_name")
    private String user2Name;

    @Column(name = "badword_count")
    private int badwordCount = 0;

    @Column(name = "leave_count")
    private int leaveCount = 0;
}