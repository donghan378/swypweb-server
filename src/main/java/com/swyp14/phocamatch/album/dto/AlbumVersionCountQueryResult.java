package com.swyp14.phocamatch.album.dto;

public record AlbumVersionCountQueryResult(
        Long versionId,
        String versionName,
        Long ownedCount,
        Long totalCount
) {
}
