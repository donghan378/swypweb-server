package com.swyp14.phocamatch.chat.dto;

public record ChatRoomCreateResponse(
        Long chatRoomId,
        Long tradeProposalId,
        String partnerNickname,
        String partnerProfileImageUrl
) {
}
