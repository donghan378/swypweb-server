package com.swyp14.phocamatch.chat.dto;

import java.util.List;

public record ChatRoomListResponse(
        List<ChatRoomListItemResponse> chats,
        String nextCursor,
        boolean hasNext
) {
}
