package com.swyp14.phocamatch.photocard.domain;

import com.swyp14.phocamatch.album.domain.AlbumVersion;
import com.swyp14.phocamatch.collection.domain.Collection;
import com.swyp14.phocamatch.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "photo_cards",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_photo_cards_version_member_card_name",
                        columnNames = {
                                "version_id",
                                "member_id",
                                "card_name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_photo_cards_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_photo_cards_version_id",
                        columnList = "version_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_photo_cards_member"
            )
    )
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "version_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_photo_cards_version"
            )
    )
    private AlbumVersion version;

    @Column(
            name = "card_name",
            nullable = false,
            length = 255
    )
    private String name;

    @Column(
            name = "card_image_url",
            nullable = false,
            length = 1000
    )
    private String imageUrl;

    @Column(name = "variant_order", nullable = false)
    private Integer variantOrder;

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

}
