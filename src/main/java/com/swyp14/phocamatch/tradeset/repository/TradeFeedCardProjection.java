package com.swyp14.phocamatch.tradeset.repository;

import com.swyp14.phocamatch.tradeset.domain.TradeType;

public interface TradeFeedCardProjection {

    Long getTradeSetId();

    Long getTradeSetItemId();

    TradeType getTradeType();

    String getImageUrl();
}
