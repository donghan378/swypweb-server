package com.swyp14.phocamatch.chat.dto;

import com.swyp14.phocamatch.chat.domain.MessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChatMessageSendRequest(
        @NotNull
        @Positive
        Long chatRoomId,

        @NotNull
        MessageType type,

        String content,

        String imageUrl
) {
}
