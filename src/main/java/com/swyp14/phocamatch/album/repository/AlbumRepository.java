package com.swyp14.phocamatch.album.repository;

import com.swyp14.phocamatch.album.domain.Album;
import com.swyp14.phocamatch.album.dto.AlbumCountQueryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("""
            SELECT new com.swyp14.phocamatch.album.dto.AlbumCountQueryResult(
                a.id,
                a.name,
                COUNT(DISTINCT col.id),
                COUNT(DISTINCT pc.id)
            )
            FROM Album a
            LEFT JOIN AlbumVersion av
                ON av.album = a
            LEFT JOIN PhotoCard pc
                ON pc.version = av
            LEFT JOIN UserCollection col
                ON col.card = pc
               AND col.user.id = :userId
            WHERE a.group.id = :groupId
            GROUP BY
                a.id,
                a.name
            ORDER BY a.id ASC
            """)
    List<AlbumCountQueryResult>
    findAlbumCountsByGroupId(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId
    );
}
