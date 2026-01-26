package com.kbulkup.common.security;

import com.kbulkup.common.exception.AuthException;
import com.kbulkup.common.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.error("WebSocket 연결 실패: Authorization 헤더가 없거나 형식이 잘못되었습니다.");
                throw new AuthException(ResponseCode.AUTH_JWT_MALFORMED);
            }

            String token = authorizationHeader.substring(7);

            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    log.error("WebSocket 연결 실패: 유효하지 않은 JWT 토큰입니다.");
                    throw new AuthException(ResponseCode.AUTH_INVALID_TOKEN);
                }

                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                accessor.setUser(authentication);

                log.info("WebSocket 연결 성공: 사용자 {}", authentication.getName());

            } catch (AuthException e) {
                log.error("WebSocket JWT 검증 실패: {}", e.getMessage());
                throw e;
            }
        }

        return message;
    }
}
