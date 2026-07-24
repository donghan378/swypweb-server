package com.swyp14.phocamatch.tradeset.dto;

import com.swyp14.phocamatch.tradeset.domain.TradeType;

public record TradeSetCardQueryResult(
        Long photoCardId,
        String albumName,
        String versionName,
        String photoCardName,
        String imageUrl,
        TradeType tradeType
) {
}
