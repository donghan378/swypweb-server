package com.swyp14.phocamatch.tradeset.dto;

import com.swyp14.phocamatch.tradeset.domain.TradeType;

public record TradeSetTypeKey(
        Long tradeSetId,
        TradeType tradeType
) {
}
