package com.swyp14.phocamatch.album.repository;

import com.swyp14.phocamatch.album.domain.AlbumVersion;
import com.swyp14.phocamatch.album.dto.AlbumVersionCountQueryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumVersionRepository extends JpaRepository<AlbumVersion, Long> {

    @Query("""
            SELECT new com.swyp14.phocamatch.album.dto.AlbumVersionCountQueryResult(
                av.id,
                av.name,
                COUNT(DISTINCT col.id),
                COUNT(DISTINCT pc.id)
            )
            FROM AlbumVersion av
            LEFT JOIN PhotoCard pc
                ON pc.version = av
            LEFT JOIN UserCollection col
                ON col.card = pc
               AND col.user.id = :userId
            WHERE av.album.id = :albumId
            GROUP BY
                av.id,
                av.name
            ORDER BY av.id ASC
            """)
    List<AlbumVersionCountQueryResult>
    findVersionCountsByAlbumId(
            @Param("albumId") Long albumId,
            @Param("userId") Long userId
    );
}
