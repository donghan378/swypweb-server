package com.swyp14.phocamatch.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRoomHeaderResponse(
        Long chatId,
        String partnerNickname,

        @JsonProperty("isCompleted")
        boolean completed,

        ChatRepresentativeCardResponse representHaveCardInfo,
        ChatRepresentativeCardResponse representWantCardInfo,

        long haveCardCount,
        long wantCardCount
) {
}
