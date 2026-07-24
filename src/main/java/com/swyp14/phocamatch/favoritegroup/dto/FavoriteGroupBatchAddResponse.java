package com.swyp14.phocamatch.favoritegroup.dto;

import java.util.List;

public record FavoriteGroupBatchAddResponse(
        List<Long> groupIds
) {
}
