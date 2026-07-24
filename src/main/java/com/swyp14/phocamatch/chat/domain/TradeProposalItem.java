package com.swyp14.phocamatch.chat.domain;

import com.swyp14.phocamatch.photocard.domain.PhotoCard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "trade_proposal_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trade_proposal_items_proposal_card",
                        columnNames = {
                                "trade_proposal_id",
                                "card_id"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeProposalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_proposal_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trade_proposal_id",
            nullable = false
    )
    private TradeProposal tradeProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "card_id",
            nullable = false
    )
    private PhotoCard card;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "proposal_type",
            nullable = false,
            length = 30
    )
    private ProposalCardType proposalType;

    public static TradeProposalItem create(
            TradeProposal tradeProposal,
            PhotoCard card,
            ProposalCardType proposalType
    ) {
        TradeProposalItem item =
                new TradeProposalItem();

        item.tradeProposal = tradeProposal;
        item.card = card;
        item.proposalType = proposalType;

        return item;
    }
}
