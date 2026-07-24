package com.swyp14.phocamatch.album.dto;

import java.util.List;

public record GroupAlbumListResponse(
        Long groupId,
        String groupName,
        long ownedCount,
        long totalCount,
        List<AlbumSummaryResponse> albums
) {
}
