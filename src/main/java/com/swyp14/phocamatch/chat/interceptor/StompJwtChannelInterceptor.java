package com.swyp14.phocamatch.chat.interceptor;

import com.swyp14.phocamatch.auth.token.TokenService;
import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.repository.ChatRoomMemberRepository;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION =
            "Authorization";

    private static final String BEARER_PREFIX =
            "Bearer ";

    private static final Pattern
            CHAT_ROOM_SUBSCRIBE_PATTERN =
            Pattern.compile(
                    "^/sub/chat/rooms/(\\d+)$"
            );

    private final TokenService tokenService;

    private final ChatRoomMemberRepository
            chatRoomMemberRepository;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        StompCommand command =
                accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            validateSubscription(accessor);
        }

        return message;
    }

    private void authenticate(
            StompHeaderAccessor accessor
    ) {
        String authorization =
                accessor.getFirstNativeHeader(
                        AUTHORIZATION
                );

        if (
                authorization == null
                        || !authorization.startsWith(
                        BEARER_PREFIX
                )
        ) {
            throw new IllegalArgumentException(
                    "WebSocket 인증 토큰이 없습니다."
            );
        }

        String accessToken =
                authorization.substring(
                        BEARER_PREFIX.length()
                );

        TokenService.AccessTokenPayload payload =
                tokenService.parseAccessToken(
                        accessToken
                );

        Long userId =
                payload.userId();

        UsernamePasswordAuthenticationToken
                authentication =
                new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        null,
                        List.of()
                );

        accessor.setUser(authentication);
    }

    private void validateSubscription(
            StompHeaderAccessor accessor
    ) {
        Principal principal =
                accessor.getUser();

        if (principal == null) {
            throw new IllegalArgumentException(
                    "인증되지 않은 WebSocket 연결입니다."
            );
        }

        String destination =
                accessor.getDestination();

        if (destination == null) {
            throw new IllegalArgumentException(
                    "구독 주소가 없습니다."
            );
        }

        Matcher matcher =
                CHAT_ROOM_SUBSCRIBE_PATTERN
                        .matcher(destination);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "허용되지 않은 구독 주소입니다."
            );
        }

        Long chatRoomId =
                Long.valueOf(
                        matcher.group(1)
                );

        Long userId =
                Long.valueOf(
                        principal.getName()
                );

        boolean participant =
                chatRoomMemberRepository
                        .existsByChatRoom_IdAndUser_Id(
                                chatRoomId,
                                userId
                        );

        if (!participant) {
            throw new ChatRoomAccessDeniedException();
        }
    }
}
