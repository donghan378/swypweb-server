package com.swyp14.phocamatch.chat.repository;

import com.swyp14.phocamatch.chat.domain.TradeProposalItem;
import com.swyp14.phocamatch.chat.dto.TradeProposalCardProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeProposalItemRepository extends JpaRepository<TradeProposalItem, Long> {

    @Query("""
            SELECT
                item.id AS proposalItemId,
                item.proposalType AS proposalType,
                item.card.id AS photoCardId,
                item.card.name AS photoCardName,
                item.card.version.album.name AS albumName,
                item.card.version.name AS versionName,
                item.card.imageUrl AS imageUrl
            FROM TradeProposalItem item
            WHERE item.tradeProposal.id = :proposalId
            ORDER BY item.id ASC
            """)
    List<TradeProposalCardProjection> findCardsByProposalId(
            @Param("proposalId") Long proposalId
    );

    @Query("""
            SELECT item
            FROM TradeProposalItem item
            JOIN FETCH item.card card
            WHERE item.tradeProposal.id = :proposalId
            ORDER BY item.id ASC
            """)
    List<TradeProposalItem> findAllByProposalId(
            @Param("proposalId") Long proposalId
    );
}
