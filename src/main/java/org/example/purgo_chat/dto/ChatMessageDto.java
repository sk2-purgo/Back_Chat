package org.example.purgo_chat.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ChatMessageDto {

    public enum MessageType {
        ENTER, TALK, LEAVE, ERROR, PARTICIPANTS
    }

    private MessageType type;
    private String roomId;
    private String sender;
    private String receiver;
    private String content;
    private String time;
    private Integer badWordCount;
    private List <String> participants;

}
