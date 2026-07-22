package com.swyp14.phocamatch.tradeset.dto;

public record TradeSetCreateResponse(
        Long tradeSetId,
        Long groupId,
        long haveCount,
        long wantCount
) {
}
