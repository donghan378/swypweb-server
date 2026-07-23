package com.swyp14.phocamatch.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ChatRoomListItemResponse(
        Long chatId,
        String partnerNickname,
        String partnerProfileImageUrl,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long unreadCount,

        @JsonProperty("isCompleted")
        boolean completed
) {
}
