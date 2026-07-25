package com.swyp14.phocamatch.tradeset.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TradeFeedItemResponse(
        Long tradeSetId,
        Long groupId,
        String groupName,
        Long userId,
        String nickname,
        List<String> haveImages,
        List<String> wantImages,
        LocalDateTime createdAt
) {
}
