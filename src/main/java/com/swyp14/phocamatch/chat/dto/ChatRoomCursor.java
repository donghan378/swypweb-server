package com.swyp14.phocamatch.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomCursor(
        LocalDateTime lastMessageAt,
        Long chatRoomId
) {
}
