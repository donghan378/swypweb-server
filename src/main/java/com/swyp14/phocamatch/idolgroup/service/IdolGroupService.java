package com.swyp14.phocamatch.idolgroup.service;

import com.swyp14.phocamatch.favoritegroup.repository.FavoriteGroupRepository;
import com.swyp14.phocamatch.favoritegroup.service.FavoriteGroupService;
import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.idolgroup.dto.IdolGroupItemResponse;
import com.swyp14.phocamatch.idolgroup.dto.IdolGroupListResponse;
import com.swyp14.phocamatch.idolgroup.repository.IdolGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IdolGroupService {

    private final IdolGroupRepository idolGroupRepository;
    private final FavoriteGroupRepository favoriteGroupRepository;

    @Transactional(readOnly = true)
    public IdolGroupListResponse getAllGroups(
            Long userId,
            Long cursor,
            int size
    ) {
        PageRequest pageRequest =
                PageRequest.of(0, size + 1);

        List<IdolGroup> fetchedGroups =
                cursor == null
                        ? idolGroupRepository
                        .findAllByOrderByIdAsc(pageRequest)
                        : idolGroupRepository
                        .findByIdGreaterThanOrderByIdAsc(
                                cursor,
                                pageRequest
                        );

        boolean hasNext =
                fetchedGroups.size() > size;

        List<IdolGroup> pageContent =
                hasNext
                        ? fetchedGroups.subList(0, size)
                        : fetchedGroups;

        if (pageContent.isEmpty()) {
            return new IdolGroupListResponse(
                    List.of(),
                    null,
                    false
            );
        }

        List<Long> groupIds =
                pageContent.stream()
                        .map(IdolGroup::getId)
                        .toList();

        Set<Long> favoriteGroupIds =
                new HashSet<>(
                        favoriteGroupRepository
                                .findFavoriteGroupIds(
                                        userId,
                                        groupIds
                                )
                );

        List<IdolGroupItemResponse> groups =
                pageContent.stream()
                        .map(group ->
                                new IdolGroupItemResponse(
                                        group.getId(),
                                        group.getName(),
                                        group.getImageUrl(),
                                        favoriteGroupIds.contains(
                                                group.getId()
                                        )
                                )
                        )
                        .toList();

        Long nextCursor =
                hasNext
                        ? pageContent
                        .get(pageContent.size() - 1)
                        .getId()
                        : null;

        return new IdolGroupListResponse(
                groups,
                nextCursor,
                hasNext
        );
    }
}
