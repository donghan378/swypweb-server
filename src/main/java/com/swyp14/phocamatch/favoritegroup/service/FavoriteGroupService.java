package com.swyp14.phocamatch.favoritegroup.service;

import com.swyp14.phocamatch.favoritegroup.domain.FavoriteGroup;
import com.swyp14.phocamatch.favoritegroup.dto.*;
import com.swyp14.phocamatch.favoritegroup.exception.InvalidGroupIdsException;
import com.swyp14.phocamatch.favoritegroup.repository.FavoriteGroupRepository;
import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.idolgroup.repository.IdolGroupRepository;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.exception.UserNotFoundException;
import com.swyp14.phocamatch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteGroupService {

    private final FavoriteGroupRepository favoriteGroupRepository;
    private final IdolGroupRepository idolGroupRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public FavoriteGroupBatchAddResponse
    addMyFavoriteGroups(
            Long userId,
            List<Long> requestedGroupIds
    ) {
        List<Long> groupIds =
                removeDuplicates(requestedGroupIds);

        User user = userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<IdolGroup> foundGroups =
                idolGroupRepository.findAllById(groupIds);

        validateAllGroupsExist(
                groupIds,
                foundGroups
        );

        Set<Long> existingFavoriteGroupIds =
                new HashSet<>(
                        favoriteGroupRepository
                                .findFavoriteGroupIds(
                                        userId,
                                        groupIds
                                )
                );

        Map<Long, IdolGroup> groupMap =
                foundGroups.stream()
                        .collect(
                                Collectors.toMap(
                                        IdolGroup::getId,
                                        Function.identity()
                                )
                        );

        List<FavoriteGroup> newFavoriteGroups =
                groupIds.stream()
                        .filter(groupId ->
                                !existingFavoriteGroupIds
                                        .contains(groupId)
                        )
                        .map(groupMap::get)
                        .map(group ->
                                FavoriteGroup.create(
                                        user,
                                        group
                                )
                        )
                        .toList();

        favoriteGroupRepository.saveAll(
                newFavoriteGroups
        );

        return new FavoriteGroupBatchAddResponse(
                groupIds
        );
    }

    private List<Long> removeDuplicates(
            List<Long> groupIds
    ) {
        return List.copyOf(
                new LinkedHashSet<>(groupIds)
        );
    }

    private void validateAllGroupsExist(
            List<Long> requestedGroupIds,
            List<IdolGroup> foundGroups
    ) {
        Set<Long> foundGroupIds =
                foundGroups.stream()
                        .map(IdolGroup::getId)
                        .collect(Collectors.toSet());

        List<Long> invalidGroupIds =
                requestedGroupIds.stream()
                        .filter(groupId ->
                                !foundGroupIds.contains(groupId)
                        )
                        .toList();

        if (!invalidGroupIds.isEmpty()) {
            throw new InvalidGroupIdsException(
                    invalidGroupIds
            );
        }
    }

}
