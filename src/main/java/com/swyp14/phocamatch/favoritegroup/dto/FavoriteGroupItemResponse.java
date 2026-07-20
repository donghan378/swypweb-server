package com.swyp14.phocamatch.favoritegroup.dto;

import java.time.LocalDateTime;

public record FavoriteGroupItemResponse(
        Long groupId,
        String name,
        String groupImageUrl,
        LocalDateTime interestedAt
) {
}
