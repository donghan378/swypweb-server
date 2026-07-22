package com.swyp14.phocamatch.tradeset.dto;

import com.swyp14.phocamatch.tradeset.domain.TradeType;

public record TradeSetTypeCountQueryResult(
        Long tradeSetId,
        TradeType tradeType,
        Long itemCount
) {
}
