package com.swyp14.phocamatch.chat.dto;

import jakarta.validation.constraints.NotNull;

public record TradeCompleteRequest(
        @NotNull(message = "카드 삭제 여부는 필수입니다.")
        Boolean deleteTradedCards
) {
}
