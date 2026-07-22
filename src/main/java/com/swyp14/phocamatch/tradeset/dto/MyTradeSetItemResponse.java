package com.swyp14.phocamatch.tradeset.dto;

import java.time.LocalDateTime;

public record MyTradeSetItemResponse(
        Long tradeSetId,
        Long groupId,

        long haveCount,
        long wantCount,

        String haveImage,
        String haveRepresentAlbumName,
        String haveRepresentVersionName,

        String wantImage,
        String wantRepresentAlbumName,
        String wantRepresentVersionName,

        LocalDateTime createdAt
) {
}
