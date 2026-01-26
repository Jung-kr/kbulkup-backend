package com.kbulkup.chat.service;

import com.kbulkup.chat.dto.ChatMessageDTO;
import com.kbulkup.chat.domain.MongoChatMessage;
import com.kbulkup.chat.dto.ChatSummaryDTO;
import com.kbulkup.chat.repository.ChatMongoRepository;
import com.kbulkup.common.exception.CounselingException;
import com.kbulkup.common.response.ResponseCode;
import com.kbulkup.counseling.domain.CounselingReservation;
import com.kbulkup.counseling.domain.ReservationStatus;
import com.kbulkup.counseling.mapper.CounselingReservationMapper;
import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRetryService chatRetryService;
    private final ChatMongoRepository chatMongoRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CounselingReservationMapper counselingReservationMapper;

    @Override
    @Async("chatExecutor")
    public void saveChatMessage(ChatMessageDTO dto, String receiverId) {
        // 1. MongoDB 채팅 내역 저장
        chatRetryService.saveChatHistory(dto, receiverId);

        // 2. MySQL 마지막 채팅, 시간 업데이트
        counselingReservationMapper.updateLatestMessage(dto.getRoomId(), dto.getMessage(), dto.getSendAt());
        log.debug("최근 메시지 업데이트 완료 - roomId: {}", dto.getRoomId());

        // 3. MongoDB 안읽은 메시지 수 카운트 하고 ChatSummary 전송
        int unreadCount = (int) chatMongoRepository.countUnreadMessages(dto.getRoomId(), receiverId);
        ChatSummaryDTO chatSummary = ChatSummaryDTO.create(dto, unreadCount, receiverId);
        messagingTemplate.convertAndSend("/queue/user/" + chatSummary.getReceiverId(), chatSummary);
        log.debug("ChatSummary 전송 완료 - receiverId: {}", receiverId);
    }

    @Override
    public String validateReservation(ChatMessageDTO dto) {
        CounselingReservation reservation = counselingReservationMapper.findByRoomId(dto.getRoomId());

        // 예약 상태가 ACTIVE인지 확인
        if (!ReservationStatus.ACTIVE.getName().equals(reservation.getStatus())) {
            throw new CounselingException(ResponseCode.CHAT_NOT_AVAILABLE);
        }

        return dto.getSenderId().equals(reservation.getTraineeId().toString())
                ? reservation.getTrainerId().toString() : reservation.getTraineeId().toString();
    }

    @Override
    @Transactional
    public List<MongoChatMessage> getMessagesByRoomId(String roomId, String userId) {
        chatMongoRepository.saveAll(roomId, userId);
        return chatMongoRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional
    public void MarkMessagesAsRead(String roomId, String userId) {
        //본인이 수신자이고 읽지 않은 메시지 읽음 처리
        List<MongoChatMessage> unreadMessages = chatMongoRepository.findByRoomId(roomId).stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                .peek(m -> m.setRead(true))
                .toList();

        //읽음 처리된 메시지 저장
        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(chatMongoRepository::save);
        }
    }
}
