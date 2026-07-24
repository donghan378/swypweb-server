package com.swyp14.phocamatch.collection.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CollectionBatchSaveRequest(

        @NotNull(message = "photoCardIds는 필수입니다.")
        List<
                @NotNull(message = "photoCardId는 null일 수 없습니다.")
                @Positive(message = "photoCardId는 양수여야 합니다.")
                Long
        > photoCardIds
) {
}
