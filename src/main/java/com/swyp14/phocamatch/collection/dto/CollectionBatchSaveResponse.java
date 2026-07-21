package com.swyp14.phocamatch.collection.dto;

public record CollectionBatchSaveResponse(
        Long groupId,
        Long ownedCount,
        Long totalCount
) {
}
