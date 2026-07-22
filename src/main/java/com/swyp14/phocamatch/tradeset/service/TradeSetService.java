package com.swyp14.phocamatch.tradeset.service;

import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;
import com.swyp14.phocamatch.idolgroup.exception.IdolGroupNotFoundException;
import com.swyp14.phocamatch.idolgroup.repository.IdolGroupRepository;
import com.swyp14.phocamatch.photocard.domain.PhotoCard;
import com.swyp14.phocamatch.photocard.repository.PhotoCardRepository;
import com.swyp14.phocamatch.tradeset.domain.TradeSet;
import com.swyp14.phocamatch.tradeset.domain.TradeSetItem;
import com.swyp14.phocamatch.tradeset.domain.TradeType;
import com.swyp14.phocamatch.tradeset.dto.TradeSetCreateResponse;
import com.swyp14.phocamatch.tradeset.exception.DuplicateTradeSetCardException;
import com.swyp14.phocamatch.tradeset.exception.InvalidTradeSetCardException;
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
}
