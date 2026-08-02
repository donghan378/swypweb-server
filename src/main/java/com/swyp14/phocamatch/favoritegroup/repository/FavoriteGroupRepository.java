package com.swyp14.phocamatch.favoritegroup.repository;

import com.swyp14.phocamatch.favoritegroup.domain.FavoriteGroup;
import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FavoriteGroupRepository extends JpaRepository<FavoriteGroup, Long> {

    @Query("""
            SELECT fg
            FROM FavoriteGroup fg
            JOIN FETCH fg.group g
            WHERE fg.user.id = :userId
            ORDER BY fg.id DESC 
            """)
    List<FavoriteGroup> findFirstPage(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
            SELECT fg
            FROM FavoriteGroup fg
            JOIN FETCH fg.group g
            WHERE fg.user.id = :userId
                AND fg.id < :cursor
            ORDER BY fg.id DESC
            """)
    List<FavoriteGroup> findNextPage(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT fg.group.id
            FROM FavoriteGroup fg
            WHERE fg.user.id = :userId
              AND fg.group.id IN :groupIds
            """)
    List<Long> findFavoriteGroupIds(
            @Param("userId") Long userId,
            @Param("groupIds")
            Collection<Long> groupIds
    );

    Optional<FavoriteGroup> findByUser_IdAndGroup_Id(
            Long userId,
            Long groupId
    );

}
