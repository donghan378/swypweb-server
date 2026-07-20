package com.swyp14.phocamatch.favoritegroup.domain;


import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "favorite_groups",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_favorite_groups_user_group",
                        columnNames = {
                                "user_id",
                                "group_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_favorite_groups_user_cursor",
                        columnList = "user_id, favorite_group_id"
                ),
                @Index(
                        name = "idx_favorite_groups_group_id",
                        columnList = "group_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_group_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_favorite_groups_user"
            )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "group_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_favorite_groups_group"
            )
    )
    private IdolGroup group;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public static FavoriteGroup create(
            User user,
            IdolGroup group
    ) {
        FavoriteGroup favoriteGroup =
                new FavoriteGroup();

        favoriteGroup.user = user;
        favoriteGroup.group = group;

        return favoriteGroup;
    }
}
