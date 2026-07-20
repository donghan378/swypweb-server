package com.swyp14.phocamatch.idolgroup.dto;

public record IdolGroupQueryResult(
        Long groupId,
        String name,
        String groupImageUrl,
        boolean interested
) {
}
