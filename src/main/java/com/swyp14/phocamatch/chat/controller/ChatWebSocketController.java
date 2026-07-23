package com.swyp14.phocamatch.chat.controller;

import com.swyp14.phocamatch.chat.dto.ChatMessageResponse;
import com.swyp14.phocamatch.chat.dto.ChatMessageSendRequest;
import com.swyp14.phocamatch.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/messages")
    public void sendMessage(
            ChatMessageSendRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new IllegalArgumentException(
                    "인증되지 않은 WebSocket 사용자입니다."
            );
        }

        Long userId =
                Long.valueOf(
                        principal.getName()
                );

        ChatMessageResponse response =
                chatMessageService.sendMessage(
                        userId,
                        request
                );

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/"
                        + request.chatRoomId(),
                response
        );
    }
}
