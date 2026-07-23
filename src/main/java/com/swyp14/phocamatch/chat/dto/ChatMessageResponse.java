package com.swyp14.phocamatch.chat.dto;

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
}
