package com.swyp14.phocamatch.album.dto;

public record AlbumSummaryResponse(
        Long albumId,
        String name,
        long ownedCount,
        long totalCount
) {
}
