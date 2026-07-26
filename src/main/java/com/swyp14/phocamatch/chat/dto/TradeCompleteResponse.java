package com.swyp14.phocamatch.chat.dto;

public record TradeCompleteResponse(
        Long chatId,
        boolean isCompleted,
        boolean deletedSelectedCards
) {
}
