package com.swyp14.phocamatch.collection.domain;

import com.swyp14.phocamatch.photocard.domain.PhotoCard;
import com.swyp14.phocamatch.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "UserCollection")
@Table(
        name = "collections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_collections_user_card",
                        columnNames = {
                                "user_id",
                                "card_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_collections_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_collections_card_id",
                        columnList = "card_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_collections_user"
            )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "card_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_collections_card"
            )
    )
    private PhotoCard card;

    @Column(
            name = "quantity",
            nullable = false
    )
    private int quantity;

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

    public static Collection create(
            User user,
            PhotoCard card
    ) {
        Collection collection = new Collection();

        collection.user = user;
        collection.card = card;
        collection.quantity = 1;

        return collection;
    }

    public void increaseQuantity() {
        this.quantity++;
    }

    public void decreaseQuantity() {
        if (this.quantity <= 1) {
            throw new IllegalStateException(
                    "보유 수량은 1보다 작을 수 없습니다."
            );
        }

        this.quantity--;
    }
}
