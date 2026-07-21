package com.swyp14.phocamatch.collection.service;

import com.swyp14.phocamatch.collection.domain.Collection;
import com.swyp14.phocamatch.collection.dto.CollectionBatchSaveResponse;
import com.swyp14.phocamatch.collection.exception.InvalidPhotoCardIdsException;
import com.swyp14.phocamatch.collection.repository.CollectionRepository;
import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.idolgroup.exception.IdolGroupNotFoundException;
import com.swyp14.phocamatch.idolgroup.repository.IdolGroupRepository;
import com.swyp14.phocamatch.photocard.domain.PhotoCard;
import com.swyp14.phocamatch.photocard.repository.PhotoCardRepository;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.exception.UserNotFoundException;
import com.swyp14.phocamatch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final PhotoCardRepository photoCardRepository;
    private final IdolGroupRepository idolGroupRepository;
    private final UserRepository userRepository;

    @Transactional
    public CollectionBatchSaveResponse saveGroupCollection(
            Long userId,
            Long groupId,
            List<Long> requestedPhotoCardIds
    ) {
        IdolGroup group = idolGroupRepository
                .findById(groupId)
                .orElseThrow(IdolGroupNotFoundException::new);

        User user = userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<Long> photoCardIds =
                removeDuplicates(requestedPhotoCardIds);

        List<PhotoCard> requestedPhotoCards =
                findAndValidatePhotoCards(
                        group.getId(),
                        photoCardIds
                );

        List<Collection> currentCollections =
                collectionRepository
                        .findAllByUserIdAndGroupId(
                                userId,
                                groupId
                        );

        Set<Long> requestedIdSet =
                new HashSet<>(photoCardIds);

        Set<Long> currentIdSet =
                currentCollections.stream()
                        .map(collection ->
                                collection.getCard().getId()
                        )
                        .collect(Collectors.toSet());

        List<Collection> collectionsToDelete =
                currentCollections.stream()
                        .filter(collection ->
                                !requestedIdSet.contains(
                                        collection.getCard().getId()
                                )
                        )
                        .toList();

        Map<Long, PhotoCard> requestedCardMap =
                requestedPhotoCards.stream()
                        .collect(
                                Collectors.toMap(
                                        PhotoCard::getId,
                                        Function.identity()
                                )
                        );

        List<Collection> collectionsToAdd =
                photoCardIds.stream()
                        .filter(photoCardId ->
                                !currentIdSet.contains(photoCardId)
                        )
                        .map(requestedCardMap::get)
                        .map(photoCard ->
                                Collection.create(
                                        user,
                                        photoCard
                                )
                        )
                        .toList();

        collectionRepository.deleteAllInBatch(
                collectionsToDelete
        );

        collectionRepository.saveAll(
                collectionsToAdd
        );

        long ownedCount = photoCardIds.size();

        long totalCount =
                photoCardRepository.countByGroupId(
                        groupId
                );

        return new CollectionBatchSaveResponse(
                groupId,
                ownedCount,
                totalCount
        );
    }

    private List<Long> removeDuplicates(
            List<Long> photoCardIds
    ) {
        return List.copyOf(
                new LinkedHashSet<>(photoCardIds)
        );
    }

    private List<PhotoCard> findAndValidatePhotoCards(
            Long groupId,
            List<Long> photoCardIds
    ) {
        if (photoCardIds.isEmpty()) {
            return List.of();
        }

        List<PhotoCard> foundPhotoCards =
                photoCardRepository
                        .findAllByGroupIdAndIdIn(
                                groupId,
                                photoCardIds
                        );

        Set<Long> foundIds =
                foundPhotoCards.stream()
                        .map(PhotoCard::getId)
                        .collect(Collectors.toSet());

        List<Long> invalidIds =
                photoCardIds.stream()
                        .filter(photoCardId ->
                                !foundIds.contains(photoCardId)
                        )
                        .toList();

        if (!invalidIds.isEmpty()) {
            throw new InvalidPhotoCardIdsException(
                    invalidIds
            );
        }

        return foundPhotoCards;
    }
}
