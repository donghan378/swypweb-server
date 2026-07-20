package com.swyp14.phocamatch.idolgroup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "idol_groups",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_idol_groups_group_name",
                        columnNames = "group_name"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdolGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(
            name = "group_name",
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            name = "group_image_url",
            length = 1000
    )
    private String imageUrl;

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
