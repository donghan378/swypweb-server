package com.swyp14.phocamatch.tradeset.dto;

import java.util.List;

public record TradeFeedResponse(
        List<TradeFeedItemResponse> feed,
        Long nextCursor,
        boolean hasNext
) {
}
