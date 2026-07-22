package com.swyp14.phocamatch.tradeset.domain;

import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "trade_sets",
        indexes = {
                @Index(
                        name = "idx_trade_sets_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_trade_sets_group_id",
                        columnList = "group_id"
                ),
                @Index(
                        name = "idx_trade_sets_status",
                        columnList = "status"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_set_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_trade_sets_user"
            )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "group_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_trade_sets_group"
            )
    )
    private IdolGroup group;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private TradeSetStatus status;

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

    public static TradeSet create(
            User user,
            IdolGroup group
    ) {
        TradeSet tradeSet = new TradeSet();

        tradeSet.user = user;
        tradeSet.group = group;
        tradeSet.status = TradeSetStatus.ACTIVE;

        return tradeSet;
    }

}
