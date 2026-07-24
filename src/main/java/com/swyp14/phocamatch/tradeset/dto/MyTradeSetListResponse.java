package com.swyp14.phocamatch.tradeset.dto;

import java.util.List;

public record MyTradeSetListResponse(
        List<MyTradeSetItemResponse> tradeSets
) {
}
