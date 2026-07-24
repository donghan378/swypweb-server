package com.swyp14.phocamatch.chat.domain;

import com.swyp14.phocamatch.tradeset.domain.TradeSet;
import com.swyp14.phocamatch.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trade_proposals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_proposal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "target_trade_set_id",
            nullable = false
    )
    private TradeSet targetTradeSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "proposer_user_id",
            nullable = false
    )
    private User proposer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "receiver_user_id",
            nullable = false
    )
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private TradeProposalStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static TradeProposal create(
            TradeSet targetTradeSet,
            User proposer,
            User receiver
    ) {
        TradeProposal proposal =
                new TradeProposal();

        proposal.targetTradeSet = targetTradeSet;
        proposal.proposer = proposer;
        proposal.receiver = receiver;
        proposal.status = TradeProposalStatus.PENDING;

        return proposal;
    }
}
