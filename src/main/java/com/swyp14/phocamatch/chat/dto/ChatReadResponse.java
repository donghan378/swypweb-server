package com.swyp14.phocamatch.chat.dto;

public record ChatReadResponse(
        Long chatId,
        long unreadCount
) {
}
