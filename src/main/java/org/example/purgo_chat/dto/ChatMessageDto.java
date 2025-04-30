package org.example.purgo_chat.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private MessageType type;
    private String roomId;
    private String sender;
    private String receiver;
    private String content;
    private String time;

    public enum MessageType {
        ENTER, TALK, LEAVE
    }
}
