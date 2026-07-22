package com.swyp14.phocamatch.tradeset.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TradeSetDetailResponse(
        Long tradeSetId,
        Long groupId,
        String groupName,
        LocalDateTime createdAt,
        List<TradeSetCardResponse> haveCards,
        List<TradeSetCardResponse> wantCards
) {
}
