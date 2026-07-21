package com.swyp14.phocamatch.photocard.repository;

import com.swyp14.phocamatch.photocard.domain.PhotoCard;
import com.swyp14.phocamatch.photocard.dto.PhotoCardQueryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoCardRepository extends JpaRepository<PhotoCard, Long> {

    @Query("""
            SELECT new com.swyp14.phocamatch.photocard.dto.PhotoCardQueryResult(
                pc.id,
                pc.name,
                pc.member.name,
                pc.imageUrl,
                CASE
                    WHEN COUNT(col.id) > 0 THEN true
                    ELSE false
                END
            )
            FROM PhotoCard pc
            LEFT JOIN UserCollection col
                ON col.card = pc
               AND col.user.id = :userId
            WHERE pc.version.id = :versionId
            GROUP BY
                pc.id,
                pc.name,
                pc.member.name,
                pc.imageUrl
            ORDER BY
                pc.member.id ASC,
                pc.id ASC
            """)
    List<PhotoCardQueryResult> findPhotoCardsByVersionId(
            @Param("versionId") Long versionId,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT pc
            FROM PhotoCard pc
            WHERE pc.version.album.group.id = :groupId
              AND pc.id IN :photoCardIds
            """)
    List<PhotoCard> findAllByGroupIdAndIdIn(
            @Param("groupId") Long groupId,
            @Param("photoCardIds") List<Long> photoCardIds
    );

    @Query("""
            SELECT COUNT(pc.id)
            FROM PhotoCard pc
            WHERE pc.version.album.group.id = :groupId
            """)
    long countByGroupId(
            @Param("groupId") Long groupId
    );
}
