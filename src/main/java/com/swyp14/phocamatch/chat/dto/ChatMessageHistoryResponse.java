package com.swyp14.phocamatch.chat.dto;

import java.util.List;

public record ChatMessageHistoryResponse(
        List<ChatMessageResponse> messages,
        Long nextCursor,
        boolean hasNext
) {
}
