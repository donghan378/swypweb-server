package com.swyp14.phocamatch.chat.dto;

import com.swyp14.phocamatch.chat.domain.ProposalCardType;

public interface TradeProposalCardProjection {
    Long getProposalItemId();

    ProposalCardType getProposalType();

    Long getPhotoCardId();

    String getPhotoCardName();

    String getAlbumName();

    String getVersionName();

    String getImageUrl();
}
