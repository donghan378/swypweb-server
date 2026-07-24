package com.swyp14.phocamatch.chat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ChatRoomCreateRequest(
        @NotNull(message = "targetTradeSetId는 필수입니다.")
        @Positive(message = "targetTradeSetId는 양수여야 합니다.")
        Long targetTradeSetId,

        @NotEmpty(message = "받을 카드를 한 장 이상 선택해야 합니다.")
        List<
                        @NotNull
                        @Positive
                                Long
                        > receiveCardIds,

        @NotEmpty(message = "줄 카드를 한 장 이상 선택해야 합니다.")
        List<
                @NotNull
                @Positive
                        Long
                > giveCardIds
) {
}
