package com.swyp14.phocamatch.album.service;

import com.swyp14.phocamatch.album.domain.Album;
import com.swyp14.phocamatch.album.dto.*;
import com.swyp14.phocamatch.album.exception.AlbumNotFoundException;
import com.swyp14.phocamatch.album.repository.AlbumRepository;
import com.swyp14.phocamatch.album.repository.AlbumVersionRepository;
import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.idolgroup.exception.IdolGroupNotFoundException;
import com.swyp14.phocamatch.idolgroup.repository.IdolGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final IdolGroupRepository idolGroupRepository;
    private final AlbumRepository albumRepository;
    private final AlbumVersionRepository albumVersionRepository;

    @Transactional(readOnly = true)
    public GroupAlbumListResponse getGroupAlbums(
            Long userId,
            Long groupId
    ) {
        IdolGroup group = idolGroupRepository
                .findById(groupId)
                .orElseThrow(
                        IdolGroupNotFoundException::new
                );

        List<AlbumCountQueryResult> queryResults =
                albumRepository.findAlbumCountsByGroupId(
                        groupId,
                        userId
                );


        List<AlbumSummaryResponse> albums =
                queryResults.stream()
                        .map(result ->
                                new AlbumSummaryResponse(
                                        result.albumId(),
                                        result.albumName(),
                                        result.ownedCount(),
                                        result.totalCount()
                                )
                        )
                        .toList();

        long groupOwnedCount =
                queryResults.stream()
                        .mapToLong(
                                AlbumCountQueryResult::ownedCount
                        )
                        .sum();

        long groupTotalCount =
                queryResults.stream()
                        .mapToLong(
                                AlbumCountQueryResult::totalCount
                        )
                        .sum();

        return new GroupAlbumListResponse(
                group.getId(),
                group.getName(),
                groupOwnedCount,
                groupTotalCount,
                albums
        );
    }

    @Transactional(readOnly = true)
    public AlbumVersionListResponse getAlbumVersions(
            Long userId,
            Long albumId
    ) {
        Album album = albumRepository
                .findById(albumId)
                .orElseThrow(AlbumNotFoundException::new);

        List<AlbumVersionCountQueryResult> queryResults =
                albumVersionRepository
                        .findVersionCountsByAlbumId(
                                albumId,
                                userId
                        );

        List<AlbumVersionSummaryResponse> versions =
                queryResults.stream()
                        .map(result ->
                                new AlbumVersionSummaryResponse(
                                        result.versionId(),
                                        result.versionName(),
                                        result.ownedCount(),
                                        result.totalCount()
                                )
                        )
                        .toList();

        long albumOwnedCount =
                queryResults.stream()
                        .mapToLong(
                                AlbumVersionCountQueryResult::ownedCount
                        )
                        .sum();

        long albumTotalCount =
                queryResults.stream()
                        .mapToLong(
                                AlbumVersionCountQueryResult::totalCount
                        )
                        .sum();

        return new AlbumVersionListResponse(
                album.getId(),
                album.getName(),
                albumOwnedCount,
                albumTotalCount,
                versions
        );
    }

}
