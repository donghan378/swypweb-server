package com.swyp14.phocamatch.tradeset.dto;

public record TradeSetCardResponse(
        Long photoCardId,
        String albumName,
        String versionName,
        String photoCardName,
        String imageUrl
) {
}
