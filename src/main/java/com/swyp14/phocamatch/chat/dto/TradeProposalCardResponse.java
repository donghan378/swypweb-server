package com.swyp14.phocamatch.chat.dto;

public record TradeProposalCardResponse(
        Long photoCardId,
        String photoCardName,
        String albumName,
        String versionName,
        String imageUrl
) {
}
