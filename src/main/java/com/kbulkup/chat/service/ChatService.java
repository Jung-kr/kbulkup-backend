package com.kbulkup.chat.service;

import com.kbulkup.chat.dto.ChatMessageDTO;
import com.kbulkup.chat.domain.MongoChatMessage;
import com.kbulkup.chat.dto.ChatSummaryDTO;

import java.util.List;

public interface ChatService {

    void saveChatMessage(ChatMessageDTO chatMessageDTO, String receiverId);

    String validateReservation(ChatMessageDTO chatMessageDTO);

    List<MongoChatMessage> getMessagesByRoomId(String roomId, String userId);

    void MarkMessagesAsRead(String roomId, String userId);
}
