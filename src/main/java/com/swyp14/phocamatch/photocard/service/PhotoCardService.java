package com.swyp14.phocamatch.photocard.service;

import com.swyp14.phocamatch.album.domain.AlbumVersion;
import com.swyp14.phocamatch.album.exception.AlbumVersionNotFoundException;
import com.swyp14.phocamatch.album.repository.AlbumVersionRepository;
import com.swyp14.phocamatch.photocard.dto.PhotoCardItemResponse;
import com.swyp14.phocamatch.photocard.dto.PhotoCardListResponse;
import com.swyp14.phocamatch.photocard.dto.PhotoCardQueryResult;
import com.swyp14.phocamatch.photocard.repository.PhotoCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoCardService {

    private final AlbumVersionRepository albumVersionRepository;
    private final PhotoCardRepository photoCardRepository;

    @Transactional(readOnly = true)
    public PhotoCardListResponse getPhotoCardsByVersion(
            Long userId,
            Long versionId
    ) {
        AlbumVersion version = albumVersionRepository
                .findById(versionId)
                .orElseThrow(
                        AlbumVersionNotFoundException::new
                );

        List<PhotoCardQueryResult> queryResults =
                photoCardRepository.findPhotoCardsByVersionId(
                        versionId,
                        userId
                );

        List<PhotoCardItemResponse> photoCards =
                queryResults.stream()
                        .map(result ->
                                new PhotoCardItemResponse(
                                        result.photoCardId(),
                                        result.photoCardName(),
                                        result.memberName(),
                                        result.imageUrl(),
                                        Boolean.TRUE.equals(
                                                result.owned()
                                        )
                                )
                        )
                        .toList();

        long ownedCount =
                queryResults.stream()
                        .filter(result ->
                                Boolean.TRUE.equals(
                                        result.owned()
                                )
                        )
                        .count();

        long totalCount = queryResults.size();

        return new PhotoCardListResponse(
                version.getId(),
                version.getName(),
                ownedCount,
                totalCount,
                photoCards
        );
    }
}
