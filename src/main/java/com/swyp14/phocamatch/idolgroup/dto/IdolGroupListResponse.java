package com.swyp14.phocamatch.idolgroup.dto;

import java.util.List;

public record IdolGroupListResponse(
        List<IdolGroupItemResponse> groups,
        Long nextCursor,
        boolean hasNext
) {
}
