package com.swyp14.phocamatch.tradeset.dto;

import java.time.LocalDateTime;

public record TradeSetMatchCursor(
        Long matchScore,
        LocalDateTime createdAt,
        Long tradeSetId
) {
}
