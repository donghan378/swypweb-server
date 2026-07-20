package com.swyp14.phocamatch.idolgroup.dto;

public record IdolGroupItemResponse(
        Long groupId,
        String name,
        String groupImageUrl,
        boolean interested
) {
}
