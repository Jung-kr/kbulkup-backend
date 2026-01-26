package com.kbulkup.chat.service;

import com.kbulkup.chat.domain.MongoChatMessage;
import com.kbulkup.chat.dto.ChatMessageDTO;
import com.kbulkup.chat.repository.ChatMongoRepository;
import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRetryService {

    private final ChatMongoRepository chatMongoRepository;

    @Retryable(
            value = {MongoException.class, DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public void saveChatHistory(ChatMessageDTO dto, String receiverId) {
        MongoChatMessage document = MongoChatMessage.create(dto, receiverId);
        chatMongoRepository.save(document);
        log.debug("채팅 내역 저장 완료 - roomId: {}", dto.getRoomId());
    }

    @Recover
    public void recoverSaveChatHistory(Exception e, ChatMessageDTO dto, String receiverId) {
        log.error("채팅 저장 최종 실패 - roomId: {}", dto.getRoomId(), e);
        // TODO: Redis Queue에 저장
    }
}
