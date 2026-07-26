package com.swyp14.phocamatch.chat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record TradeCompleteRequest(
        @NotEmpty(message = "교환한 내 포토카드를 선택해야 합니다.")
        List<
                @NotNull
                @Positive
                        Long
                > myCardIds,

        @NotEmpty(message = "교환한 상대방 포토카드를 선택해야 합니다.")
        List<
                        @NotNull
                        @Positive
                                Long
                        > partnerCardIds,

        @NotNull(message = "포토카드 삭제 여부는 필수입니다.")
        Boolean deleteSelectedCards
) {
}
