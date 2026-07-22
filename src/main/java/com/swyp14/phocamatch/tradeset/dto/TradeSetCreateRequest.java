package com.swyp14.phocamatch.tradeset.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record TradeSetCreateRequest(

        @NotEmpty(message = "있어요 카드를 한 개 이상 선택해야 합니다.")
        List<
                        @NotNull(message = "haveCardId는 null일 수 없습니다.")
                        @Positive(message = "haveCardId는 양수여야 합니다.")
                        Long
                        > haveCardIds,

        @NotEmpty(message = "원해요 카드를 한 개 이상 선택해야 합니다.")
        List<
                @NotNull(message = "wantCardId는 null일 수 없습니다.")
                @Positive(message = "wantCardId는 양수여야 합니다.")
                Long
                >wantCardIds
) {
}
