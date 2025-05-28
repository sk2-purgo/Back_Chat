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

    @Column(name = "user3_name")
    private String user3Name;

    @Column(name = "user4_name")
    private String user4Name;

    @Column(name = "user5_name")
    private String user5Name;

    @Column(name = "user6_name")
    private String user6Name;

    @Column(name = "user7_name")
    private String user7Name;

    @Column(name = "user8_name")
    private String user8Name;

    @Column(name = "badword_count")
    private int badwordCount = 0;

    @Column(name = "leave_count")
    private int leaveCount = 0;
}