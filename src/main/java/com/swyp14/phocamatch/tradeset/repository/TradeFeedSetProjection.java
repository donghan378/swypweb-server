package com.swyp14.phocamatch.tradeset.repository;

import java.time.LocalDateTime;

public interface TradeFeedSetProjection {

    Long getTradeSetId();

    Long getGroupId();

    String getGroupName();

    Long getUserId();

    String getNickname();

    String getProfileImageUrl();

    LocalDateTime getCreatedAt();
}
