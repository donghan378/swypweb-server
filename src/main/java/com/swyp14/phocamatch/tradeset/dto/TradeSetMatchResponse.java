package com.swyp14.phocamatch.tradeset.dto;

import java.util.List;

public record TradeSetMatchResponse(
        Long tradeSetId,
        Long userId,
        String nickname,
        long matchScore,
        List<MatchedPhotoCardResponse> matchedHaveCards,
        List<MatchedPhotoCardResponse> matchedWantCards
) {
}
