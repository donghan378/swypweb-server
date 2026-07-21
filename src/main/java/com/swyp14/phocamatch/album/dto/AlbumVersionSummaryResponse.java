package com.swyp14.phocamatch.album.dto;

public record AlbumVersionSummaryResponse(
        Long versionId,
        String name,
        Long ownedCount,
        Long totalCount
) {
}
