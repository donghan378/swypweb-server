package com.swyp14.phocamatch.tradeset.dto;

import java.util.List;

public record TradeSetMatchListResponse(
        List<TradeSetMatchResponse> matches,
        String nextCursor,
        boolean hasNext
) {
}
