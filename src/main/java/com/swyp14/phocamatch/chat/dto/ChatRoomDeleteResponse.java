package com.swyp14.phocamatch.chat.dto;

public record ChatRoomDeleteResponse(
        Long chatRoomId,
        boolean roomDeleted
) {
    public static ChatRoomDeleteResponse of(Long chatRoomId, boolean roomDeleted) {
        return new ChatRoomDeleteResponse(chatRoomId, roomDeleted);
    }
}
