package com.swyp14.phocamatch.chat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_rooms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_rooms_trade_proposal",
                        columnNames = "trade_proposal_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trade_proposal_id",
            nullable = false
    )
    private TradeProposal tradeProposal;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public static ChatRoom create(
            TradeProposal tradeProposal
    ) {
        ChatRoom chatRoom =
                new ChatRoom();

        chatRoom.tradeProposal = tradeProposal;

        return chatRoom;
    }
}
