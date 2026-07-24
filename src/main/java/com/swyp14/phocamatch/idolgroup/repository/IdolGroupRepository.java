package com.swyp14.phocamatch.idolgroup.repository;

import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.idolgroup.dto.IdolGroupQueryResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface IdolGroupRepository extends JpaRepository<IdolGroup, Long> {

    @Query("""
            SELECT g
            FROM IdolGroup g
            WHERE NOT EXISTS (
                SELECT fg.id
                FROM FavoriteGroup fg
                WHERE fg.user.id = :userId
                  AND fg.group.id = g.id
            )
            ORDER BY g.id ASC
            """)
    List<IdolGroup> findFirstNonFavoriteGroups(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
            SELECT g
            FROM IdolGroup g
            WHERE g.id > :cursor
              AND NOT EXISTS (
                  SELECT fg.id
                  FROM FavoriteGroup fg
                  WHERE fg.user.id = :userId
                    AND fg.group.id = g.id
              )
            ORDER BY g.id ASC
            """)
    List<IdolGroup> findNextNonFavoriteGroups(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    List<IdolGroup> findAllByOrderByIdAsc(
            Pageable pageable
    );

    List<IdolGroup> findByIdGreaterThanOrderByIdAsc(
            Long cursor,
            Pageable pageable
    );

    List<IdolGroup> findAllByIdIn(
            Collection<Long> groupIds
    );

}
