package com.swyp14.phocamatch.tradeset.dto;

import com.swyp14.phocamatch.tradeset.domain.TradeType;

public record TradeSetRepresentativeQueryResult(
        Long tradeSetItemId,
        Long tradeSetId,
        TradeType tradeType,
        String imageUrl,
        String albumName,
        String versionName
) {
}
