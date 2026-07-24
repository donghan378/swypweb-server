package com.swyp14.phocamatch.favoritegroup.dto;

import java.util.List;

public record FavoriteGroupListResponse(
        List<FavoriteGroupItemResponse> groups,
        Long newCursor,
        boolean hasNext
) {
}
