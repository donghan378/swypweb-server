package com.swyp14.phocamatch.photocard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PhotoCardItemResponse(
        Long photoCardId,
        String photoCardName,
        String memberName,
        String imageUrl,

        @JsonProperty("isOwned")
        boolean owned
) {
}
