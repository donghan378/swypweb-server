package com.swyp14.phocamatch.album.domain;

import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "albums",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_albums_group_album_name",
                        columnNames = {
                                "group_id",
                                "album_name"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "group_id",
            nullable = false
    )
    private IdolGroup group;

    @Column(
            name = "album_name",
            nullable = false,
            length = 255
    )
    private String name;

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
