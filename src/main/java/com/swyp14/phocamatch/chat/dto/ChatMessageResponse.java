package com.swyp14.phocamatch.chat.dto;

import com.swyp14.phocamatch.chat.domain.ChatMessage;
import com.swyp14.phocamatch.chat.domain.MessageType;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long senderId,
        MessageType type,
        String content,
        String imageUrl,
        LocalDateTime createdAt

) {

    public static ChatMessageResponse from(
            ChatMessage message
    ) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getMessageType(),
                message.getContent(),
                message.getImageUrl(),
                message.getCreatedAt()
        );
    }
}
