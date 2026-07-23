package com.swyp14.phocamatch.chat.dto;

import java.util.List;

public record TradeProposalDetailResponse(
        List<TradeProposalCardResponse> haveCards,
        List<TradeProposalCardResponse> wantCards
) {
}
