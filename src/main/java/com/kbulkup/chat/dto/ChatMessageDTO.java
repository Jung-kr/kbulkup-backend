package com.kbulkup.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageDTO {

    private String roomId;
    private String message;

    @Setter
    private String senderId;

    @Setter
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime sendAt;
}
