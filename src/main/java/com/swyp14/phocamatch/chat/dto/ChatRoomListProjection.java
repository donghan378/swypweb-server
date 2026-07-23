package com.swyp14.phocamatch.chat.dto;

import java.time.LocalDateTime;

public interface ChatRoomListProjection {

    Long getChatId();

    Long getPartnerUserId();

    String getPartnerNickname();

    String getPartnerProfileImageUrl();

    String getLastMessageType();

    String getLastMessageContent();

    LocalDateTime getLastMessageAt();

    Long getUnreadCount();

    String getProposalStatus();
}
