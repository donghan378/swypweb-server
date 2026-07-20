package com.swyp14.phocamatch.favoritegroup.dto;

import java.util.List;

public record NonFavoriteGroupListResponse(
        List<NonFavoriteGroupItemResponse> groups,
        Long nextCursor,
        boolean hasNext
) {
}
