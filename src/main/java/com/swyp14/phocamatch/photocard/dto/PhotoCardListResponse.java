package com.swyp14.phocamatch.photocard.dto;

import java.util.List;

public record PhotoCardListResponse(
        Long versionId,
        String versionName,
        Long ownedCount,
        Long totalCount,
        List<PhotoCardItemResponse> photoCards
) {
}
