package com.swyp14.phocamatch.album.dto;

import java.util.List;

public record AlbumVersionListResponse(
        Long albumId,
        String albumName,
        Long ownedCount,
        Long totalCount,
        List<AlbumVersionSummaryResponse> versions
) {
}
