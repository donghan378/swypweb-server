package com.swyp14.phocamatch.tradeset.dto;

import java.time.LocalDateTime;

public interface TradeSetMatchCandidateProjection {

    Long getTradeSetId();

    Long getUserId();

    String getNickname();

    String getProfileImageUrl();

    Long getMatchScore();

    LocalDateTime getCreatedAt();
}
