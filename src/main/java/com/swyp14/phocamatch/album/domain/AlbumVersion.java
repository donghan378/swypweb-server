package com.swyp14.phocamatch.album.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "album_versions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlbumVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "album_id",
            nullable = false
    )
    private Album album;

    @Column(
            name = "version_name",
            nullable = false,
            length = 255
    )
    private String name;

    @Column(name = "released_at")
    private LocalDate releasedAt;

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
