package com.swyp14.phocamatch.favoritegroup.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record FavoriteGroupBatchAddRequest(

        @NotEmpty(message = "관심 그룹을 한 개 이상 선택해야 합니다.")
        List<
                        @NotNull(message = "groupId는 null일 수 없습니다.")
                        @Positive(message = "groupId는 양수여야 합니다.")
                        Long
                >groupIds
) {
}
