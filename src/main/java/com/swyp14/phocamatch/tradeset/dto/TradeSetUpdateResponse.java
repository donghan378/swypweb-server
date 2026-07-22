package com.swyp14.phocamatch.tradeset.dto;

public record TradeSetUpdateResponse(
        Long tradeSetId,
        Long groupId,
        long haveCount,
        long wantCount
) {
}
