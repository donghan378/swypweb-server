package com.swyp14.phocamatch.album.dto;

public record AlbumCountQueryResult(
        Long albumId,
        String albumName,
        Long ownedCount,
        Long totalCount
) {
}
