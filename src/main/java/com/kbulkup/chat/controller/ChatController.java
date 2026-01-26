package com.kbulkup.chat.controller;

import com.kbulkup.chat.dto.ChatMessageDTO;
import com.kbulkup.chat.dto.ChatSummaryDTO;
import com.kbulkup.chat.service.ChatService;
import com.kbulkup.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.time.LocalDateTime;

@ApiIgnore // WebSocket 엔드포인트는 Swagger 문서에서 제외
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send-message")
    public void sendMessage(ChatMessageDTO chatMessageDTO, Principal principal) {

        Authentication authentication = (Authentication) principal;
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getUserId();

        chatMessageDTO.setSendAt(LocalDateTime.now());
        chatMessageDTO.setSenderId(userId.toString());

        String receiverId = chatService.validateReservation(chatMessageDTO);
        messagingTemplate.convertAndSend("/topic/room/" + chatMessageDTO.getRoomId(), chatMessageDTO);

        chatService.saveChatMessage(chatMessageDTO, receiverId);
    }
}
