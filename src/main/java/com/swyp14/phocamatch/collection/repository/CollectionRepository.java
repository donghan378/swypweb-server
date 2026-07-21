package com.swyp14.phocamatch.collection.repository;

import com.swyp14.phocamatch.collection.domain.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("""
            SELECT col
            FROM UserCollection col
            JOIN FETCH col.card pc
            WHERE col.user.id = :userId
              AND pc.version.album.group.id = :groupId
            """)
    List<Collection> findAllByUserIdAndGroupId(
            @Param("userId") Long userId,
            @Param("groupId") Long groupId
    );

    @Query("""
            SELECT COUNT(col.id)
            FROM UserCollection col
            WHERE col.user.id = :userId
              AND col.card.version.album.group.id = :groupId
            """)
    long countOwnedByUserIdAndGroupId(
            @Param("userId") Long userId,
            @Param("groupId") Long groupId
    );
}
