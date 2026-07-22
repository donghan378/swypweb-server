package com.swyp14.phocamatch.tradeset.service;

import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.idolgroup.exception.IdolGroupNotFoundException;
import com.swyp14.phocamatch.idolgroup.repository.IdolGroupRepository;
import com.swyp14.phocamatch.photocard.domain.PhotoCard;
import com.swyp14.phocamatch.photocard.repository.PhotoCardRepository;
import com.swyp14.phocamatch.tradeset.domain.TradeSet;
import com.swyp14.phocamatch.tradeset.domain.TradeSetItem;
import com.swyp14.phocamatch.tradeset.domain.TradeSetStatus;
import com.swyp14.phocamatch.tradeset.domain.TradeType;
import com.swyp14.phocamatch.tradeset.dto.*;
import com.swyp14.phocamatch.tradeset.exception.DuplicateTradeSetCardException;
import com.swyp14.phocamatch.tradeset.exception.InvalidTradeSetCardException;
import com.swyp14.phocamatch.tradeset.exception.TradeSetNotFoundException;
import com.swyp14.phocamatch.tradeset.repository.TradeSetItemRepository;
import com.swyp14.phocamatch.tradeset.repository.TradeSetRepository;
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
public class TradeSetService {

    private final TradeSetRepository tradeSetRepository;
    private final TradeSetItemRepository tradeSetItemRepository;
    private final IdolGroupRepository idolGroupRepository;
    private final PhotoCardRepository photoCardRepository;
    private final UserRepository userRepository;

    @Transactional
    public TradeSetCreateResponse createTradeSet(
            Long userId,
            Long groupId,
            List<Long> requestedHaveCardIds,
            List<Long> requestedWantCardIds
    ) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);

        IdolGroup group = idolGroupRepository
                .findById(groupId)
                .orElseThrow(IdolGroupNotFoundException::new);

        List<Long> haveCardIds =
                removeDuplicates(requestedHaveCardIds);

        List<Long> wantCardIds =
                removeDuplicates(requestedWantCardIds);

        validateNoOverlap(
                haveCardIds,
                wantCardIds
        );

        List<Long> allCardIds =
                mergeCardIds(
                        haveCardIds,
                        wantCardIds
                );

        List<PhotoCard> photoCards =
                photoCardRepository
                        .findAllByGroupIdAndIdIn(
                                groupId,
                                allCardIds
                        );

        validateAllCardsBelongToGroup(
                allCardIds,
                photoCards
        );

        Map<Long, PhotoCard> photoCardMap =
                photoCards.stream()
                        .collect(
                                Collectors.toMap(
                                        PhotoCard::getId,
                                        Function.identity()
                                )
                        );

        TradeSet tradeSet =
                tradeSetRepository.save(
                        TradeSet.create(
                                user,
                                group
                        )
                );

        List<TradeSetItem> items =
                new ArrayList<>();

        haveCardIds.forEach(cardId ->
                items.add(
                        TradeSetItem.create(
                                tradeSet,
                                photoCardMap.get(cardId),
                                TradeType.HAVE
                        )
                )
        );

        wantCardIds.forEach(cardId ->
                items.add(
                        TradeSetItem.create(
                                tradeSet,
                                photoCardMap.get(cardId),
                                TradeType.WANT
                        )
                )
        );

        tradeSetItemRepository.saveAll(items);

        return new TradeSetCreateResponse(
                tradeSet.getId(),
                group.getId(),
                haveCardIds.size(),
                wantCardIds.size()
        );
    }

    private List<Long> removeDuplicates(
            List<Long> cardIds
    ) {
        return List.copyOf(
                new LinkedHashSet<>(cardIds)
        );
    }

    private void validateNoOverlap(
            List<Long> haveCardIds,
            List<Long> wantCardIds
    ) {
        Set<Long> haveIdSet =
                new HashSet<>(haveCardIds);

        List<Long> duplicatedCardIds =
                wantCardIds.stream()
                        .filter(haveIdSet::contains)
                        .toList();

        if (!duplicatedCardIds.isEmpty()) {
            throw new DuplicateTradeSetCardException(
                    duplicatedCardIds
            );
        }
    }

    private List<Long> mergeCardIds(
            List<Long> haveCardIds,
            List<Long> wantCardIds
    ) {
        LinkedHashSet<Long> merged =
                new LinkedHashSet<>();

        merged.addAll(haveCardIds);
        merged.addAll(wantCardIds);

        return List.copyOf(merged);
    }

    private void validateAllCardsBelongToGroup(
            List<Long> requestedCardIds,
            List<PhotoCard> foundCards
    ) {
        Set<Long> foundCardIds =
                foundCards.stream()
                        .map(PhotoCard::getId)
                        .collect(Collectors.toSet());

        List<Long> invalidCardIds =
                requestedCardIds.stream()
                        .filter(cardId ->
                                !foundCardIds.contains(cardId)
                        )
                        .toList();

        if (!invalidCardIds.isEmpty()) {
            throw new InvalidTradeSetCardException(
                    invalidCardIds
            );
        }
    }

    @Transactional(readOnly = true)
    public MyTradeSetListResponse getMyTradeSets(
            Long userId,
            Long groupId
    ) {
        validateGroupExists(groupId);

        List<TradeSet> tradeSets =
                tradeSetRepository
                        .findAllByUser_IdAndGroup_IdAndStatusOrderByCreatedAtDescIdDesc(
                                userId,
                                groupId,
                                TradeSetStatus.ACTIVE
                        );

        if (tradeSets.isEmpty()) {
            return new MyTradeSetListResponse(
                    List.of()
            );
        }

        List<Long> tradeSetIds =
                tradeSets.stream()
                        .map(TradeSet::getId)
                        .toList();

        List<TradeSetTypeCountQueryResult> countResults =
                tradeSetItemRepository.findTypeCounts(
                        tradeSetIds
                );

        List<TradeSetRepresentativeQueryResult>
                representativeResults =
                tradeSetItemRepository
                        .findRepresentativeCandidates(
                                tradeSetIds
                        );

        Map<TradeSetTypeKey, Long> countMap =
                createCountMap(countResults);

        Map<
                TradeSetTypeKey,
                TradeSetRepresentativeQueryResult
                > representativeMap =
                createRepresentativeMap(
                        representativeResults
                );

        List<MyTradeSetItemResponse> responses =
                tradeSets.stream()
                        .map(tradeSet ->
                                createResponse(
                                        tradeSet,
                                        countMap,
                                        representativeMap
                                )
                        )
                        .toList();

        return new MyTradeSetListResponse(
                responses
        );
    }

    private void validateGroupExists(Long groupId) {
        if (!idolGroupRepository.existsById(groupId)) {
            throw new IdolGroupNotFoundException();
        }
    }

    private Map<TradeSetTypeKey, Long>
    createCountMap(
            List<TradeSetTypeCountQueryResult> results
    ) {
        Map<TradeSetTypeKey, Long> countMap =
                new LinkedHashMap<>();

        for (TradeSetTypeCountQueryResult result : results) {
            TradeSetTypeKey key =
                    new TradeSetTypeKey(
                            result.tradeSetId(),
                            result.tradeType()
                    );

            countMap.put(
                    key,
                    result.itemCount()
            );
        }

        return countMap;
    }

    private Map<
            TradeSetTypeKey,
            TradeSetRepresentativeQueryResult
            >
    createRepresentativeMap(
            List<TradeSetRepresentativeQueryResult> candidates
    ) {
        Map<
                TradeSetTypeKey,
                TradeSetRepresentativeQueryResult
                > representativeMap =
                new LinkedHashMap<>();

        for (
                TradeSetRepresentativeQueryResult candidate
                : candidates
        ) {
            TradeSetTypeKey key =
                    new TradeSetTypeKey(
                            candidate.tradeSetId(),
                            candidate.tradeType()
                    );

            /*
             * item.id 오름차순으로 조회했으므로
             * 유형별 최초 항목만 대표 카드로 선택
             */
            representativeMap.putIfAbsent(
                    key,
                    candidate
            );
        }

        return representativeMap;
    }

    private MyTradeSetItemResponse createResponse(
            TradeSet tradeSet,
            Map<TradeSetTypeKey, Long> countMap,
            Map<
                    TradeSetTypeKey,
                    TradeSetRepresentativeQueryResult
                    > representativeMap
    ) {
        Long tradeSetId = tradeSet.getId();

        TradeSetTypeKey haveKey =
                new TradeSetTypeKey(
                        tradeSetId,
                        TradeType.HAVE
                );

        TradeSetTypeKey wantKey =
                new TradeSetTypeKey(
                        tradeSetId,
                        TradeType.WANT
                );

        TradeSetRepresentativeQueryResult haveRepresentative =
                representativeMap.get(haveKey);

        TradeSetRepresentativeQueryResult wantRepresentative =
                representativeMap.get(wantKey);

        return new MyTradeSetItemResponse(
                tradeSetId,
                tradeSet.getGroup().getId(),

                countMap.getOrDefault(haveKey, 0L),
                countMap.getOrDefault(wantKey, 0L),

                haveRepresentative == null
                        ? null
                        : haveRepresentative.imageUrl(),

                haveRepresentative == null
                        ? null
                        : haveRepresentative.albumName(),

                haveRepresentative == null
                        ? null
                        : haveRepresentative.versionName(),

                wantRepresentative == null
                        ? null
                        : wantRepresentative.imageUrl(),

                wantRepresentative == null
                        ? null
                        : wantRepresentative.albumName(),

                wantRepresentative == null
                        ? null
                        : wantRepresentative.versionName(),

                tradeSet.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public TradeSetDetailResponse getTradeSetDetail(
            Long tradeSetId
    ) {
        TradeSet tradeSet =
                tradeSetRepository
                        .findDetailById(tradeSetId)
                        .orElseThrow(
                                TradeSetNotFoundException::new
                        );

        List<TradeSetCardQueryResult> cardResults =
                tradeSetItemRepository
                        .findCardsByTradeSetId(
                                tradeSetId
                        );

        List<TradeSetCardResponse> haveCards =
                cardResults.stream()
                        .filter(result ->
                                result.tradeType()
                                        == TradeType.HAVE
                        )
                        .map(this::toCardResponse)
                        .toList();

        List<TradeSetCardResponse> wantCards =
                cardResults.stream()
                        .filter(result ->
                                result.tradeType()
                                        == TradeType.WANT
                        )
                        .map(this::toCardResponse)
                        .toList();

        return new TradeSetDetailResponse(
                tradeSet.getId(),
                tradeSet.getGroup().getId(),
                tradeSet.getGroup().getName(),
                tradeSet.getCreatedAt(),
                haveCards,
                wantCards
        );
    }

    private TradeSetCardResponse toCardResponse(
            TradeSetCardQueryResult result
    ) {
        return new TradeSetCardResponse(
                result.photoCardId(),
                result.albumName(),
                result.versionName(),
                result.photoCardName(),
                result.imageUrl()
        );
    }
}
