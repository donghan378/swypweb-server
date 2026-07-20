package com.swyp14.phocamatch.favoritegroup.service;

import com.swyp14.phocamatch.favoritegroup.domain.FavoriteGroup;
import com.swyp14.phocamatch.favoritegroup.dto.FavoriteGroupItemResponse;
import com.swyp14.phocamatch.favoritegroup.dto.FavoriteGroupListResponse;
import com.swyp14.phocamatch.favoritegroup.dto.NonFavoriteGroupItemResponse;
import com.swyp14.phocamatch.favoritegroup.dto.NonFavoriteGroupListResponse;
import com.swyp14.phocamatch.favoritegroup.repository.FavoriteGroupRepository;
import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.idolgroup.repository.IdolGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteGroupService {

    private final FavoriteGroupRepository favoriteGroupRepository;
    private final IdolGroupRepository idolGroupRepository;

    @Transactional(readOnly = true)
    public FavoriteGroupListResponse getMyFavoriteGroups(
            Long userId,
            Long cursor,
            int size
    ){
        PageRequest pageRequest = PageRequest.of(
                0,
                size + 1
        );

        List<FavoriteGroup> fetchedGroups;

        if(cursor == null){
            fetchedGroups = favoriteGroupRepository
                    .findFirstPage(userId, pageRequest);
        }else{
            fetchedGroups = favoriteGroupRepository
                    .findNextPage(userId, cursor, pageRequest);
        }

        boolean hasNext = fetchedGroups.size() > size;

        List<FavoriteGroup> pageContent =
                hasNext ? fetchedGroups.subList(0, size) : fetchedGroups;

        List<FavoriteGroupItemResponse> groups =
                pageContent.stream()
                        .map(this::toResponse)
                        .toList();

        Long nextCursor = null;

        if(hasNext && !pageContent.isEmpty()){
            nextCursor = pageContent
                    .get(pageContent.size() - 1)
                    .getId();
        }

        return new FavoriteGroupListResponse(
                groups,
                nextCursor,
                hasNext
        );
    }
    @Transactional(readOnly = true)
    public NonFavoriteGroupListResponse
    getMyNonFavoriteGroups(
            Long userId,
            Long cursor,
            int size
    ) {
        PageRequest pageRequest =
                PageRequest.of(0, size + 1);

        List<IdolGroup> fetchedGroups =
                cursor == null
                        ? idolGroupRepository
                        .findFirstNonFavoriteGroups(
                                userId,
                                pageRequest
                        )
                        : idolGroupRepository
                        .findNextNonFavoriteGroups(
                                userId,
                                cursor,
                                pageRequest
                        );

        boolean hasNext =
                fetchedGroups.size() > size;

        List<IdolGroup> pageContent =
                hasNext
                        ? fetchedGroups.subList(0, size)
                        : fetchedGroups;

        List<NonFavoriteGroupItemResponse> groups =
                pageContent.stream()
                        .map(
                                NonFavoriteGroupItemResponse::from
                        )
                        .toList();

        Long nextCursor =
                hasNext && !pageContent.isEmpty()
                        ? pageContent
                        .get(pageContent.size() - 1)
                        .getId()
                        : null;

        return new NonFavoriteGroupListResponse(
                groups,
                nextCursor,
                hasNext
        );
    }


    private FavoriteGroupItemResponse toResponse(FavoriteGroup favoriteGroup){
        return new FavoriteGroupItemResponse(
                favoriteGroup.getGroup().getId(),
                favoriteGroup.getGroup().getName(),
                favoriteGroup.getGroup().getImageUrl(),
                favoriteGroup.getCreatedAt()
        );
    }
}
