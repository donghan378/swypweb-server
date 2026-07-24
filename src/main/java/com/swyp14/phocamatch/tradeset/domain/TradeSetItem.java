package com.swyp14.phocamatch.tradeset.domain;

import com.swyp14.phocamatch.photocard.domain.PhotoCard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "trade_set_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trade_set_items_set_card",
                        columnNames = {
                                "trade_set_id",
                                "card_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_trade_set_items_trade_set_id",
                        columnList = "trade_set_id"
                ),
                @Index(
                        name = "idx_trade_set_items_card_id",
                        columnList = "card_id"
                ),
                @Index(
                        name = "idx_trade_set_items_type",
                        columnList = "trade_type"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeSetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_set_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trade_set_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_trade_set_items_trade_set"
            )
    )
    private TradeSet tradeSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "card_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_trade_set_items_card"
            )
    )
    private PhotoCard card;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trade_type",
            nullable = false,
            length = 20
    )
    private TradeType tradeType;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public static TradeSetItem create(
            TradeSet tradeSet,
            PhotoCard card,
            TradeType tradeType
    ) {
        TradeSetItem item = new TradeSetItem();

        item.tradeSet = tradeSet;
        item.card = card;
        item.tradeType = tradeType;

        return item;
    }
}
