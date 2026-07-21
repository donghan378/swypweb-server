package com.swyp14.phocamatch.photocard.dto;

public record PhotoCardQueryResult(
        Long photoCardId,
        String photoCardName,
        String memberName,
        String imageUrl,
        Boolean owned
) {
}
