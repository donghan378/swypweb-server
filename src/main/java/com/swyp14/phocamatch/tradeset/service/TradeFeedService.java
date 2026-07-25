package com.swyp14.phocamatch.tradeset.service;

import com.swyp14.phocamatch.idolgroup.exception.IdolGroupNotFoundException;
import com.swyp14.phocamatch.idolgroup.repository.IdolGroupRepository;
import com.swyp14.phocamatch.tradeset.domain.TradeSetStatus;
import com.swyp14.phocamatch.tradeset.domain.TradeType;
import com.swyp14.phocamatch.tradeset.dto.TradeFeedItemResponse;
import com.swyp14.phocamatch.tradeset.dto.TradeFeedResponse;
import com.swyp14.phocamatch.tradeset.repository.TradeFeedCardProjection;
import com.swyp14.phocamatch.tradeset.repository.TradeFeedSetProjection;
import com.swyp14.phocamatch.tradeset.repository.TradeSetItemRepository;
import com.swyp14.phocamatch.tradeset.repository.TradeSetRepository;
import com.swyp14.phocamatch.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TradeFeedService {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final IdolGroupRepository idolGroupRepository;
    private final TradeSetRepository tradeSetRepository;
    private final TradeSetItemRepository tradeSetItemRepository;

    @Transactional(readOnly = true)
    public TradeFeedResponse getFeed(
            Long groupId,
            Long cursor,
            Integer requestedSize
    ) {
        int size = resolveSize(requestedSize);

        validateGroup(groupId);

        /*
         * 다음 페이지 존재 여부를 확인하기 위해
         * 요청 크기보다 한 개 더 조회한다.
         */
        List<TradeFeedSetProjection> queriedSets =
                tradeSetRepository.findFeed(
                        groupId,
                        cursor,
                        TradeSetStatus.ACTIVE,
                        UserStatus.ACTIVE,
                        PageRequest.of(0, size + 1)
                );

        boolean hasNext =
                queriedSets.size() > size;

        List<TradeFeedSetProjection> currentPage =
                hasNext
                        ? new ArrayList<>(
                        queriedSets.subList(0, size)
                )
                        : new ArrayList<>(queriedSets);

        if (currentPage.isEmpty()) {
            return new TradeFeedResponse(
                    List.of(),
                    null,
                    false
            );
        }

        List<Long> tradeSetIds =
                currentPage.stream()
                        .map(
                                TradeFeedSetProjection
                                        ::getTradeSetId
                        )
                        .toList();

        List<TradeFeedCardProjection> cardRows =
                tradeSetItemRepository
                        .findFeedCardsByTradeSetIds(
                                tradeSetIds
                        );

        Map<Long, FeedCardImages> imageMap =
                createImageMap(
                        tradeSetIds,
                        cardRows
                );

        List<TradeFeedItemResponse> feed =
                currentPage.stream()
                        .map(set -> {
                            FeedCardImages images =
                                    imageMap.get(
                                            set.getTradeSetId()
                                    );

                            return new TradeFeedItemResponse(
                                    set.getTradeSetId(),
                                    set.getGroupId(),
                                    set.getGroupName(),
                                    set.getUserId(),
                                    set.getNickname(),
                                    List.copyOf(
                                            images.haveImages()
                                    ),
                                    List.copyOf(
                                            images.wantImages()
                                    ),
                                    set.getCreatedAt()
                            );
                        })
                        .toList();

        Long nextCursor =
                hasNext
                        ? currentPage
                        .get(currentPage.size() - 1)
                        .getTradeSetId()
                        : null;

        return new TradeFeedResponse(
                feed,
                nextCursor,
                hasNext
        );
    }

    private void validateGroup(
            Long groupId
    ) {
        if (groupId == null) {
            return;
        }

        if (!idolGroupRepository.existsById(groupId)) {
            throw new IdolGroupNotFoundException();
        }
    }

    private int resolveSize(
            Integer requestedSize
    ) {
        if (requestedSize == null) {
            return DEFAULT_SIZE;
        }

        if (
                requestedSize < 1
                        || requestedSize > MAX_SIZE
        ) {
            throw new IllegalArgumentException(
                    "size는 1 이상 100 이하여야 합니다."
            );
        }

        return requestedSize;
    }

    private Map<Long, FeedCardImages> createImageMap(
            List<Long> tradeSetIds,
            List<TradeFeedCardProjection> cardRows
    ) {
        Map<Long, FeedCardImages> result =
                new LinkedHashMap<>();

        for (Long tradeSetId : tradeSetIds) {
            result.put(
                    tradeSetId,
                    new FeedCardImages(
                            new ArrayList<>(),
                            new ArrayList<>()
                    )
            );
        }

        for (TradeFeedCardProjection row : cardRows) {
            if (row.getImageUrl() == null) {
                continue;
            }

            FeedCardImages images =
                    result.get(
                            row.getTradeSetId()
                    );

            if (images == null) {
                continue;
            }

            if (row.getTradeType() == TradeType.HAVE) {
                images.haveImages()
                        .add(row.getImageUrl());
            }

            if (row.getTradeType() == TradeType.WANT) {
                images.wantImages()
                        .add(row.getImageUrl());
            }
        }

        return result;
    }

    private record FeedCardImages(
            List<String> haveImages,
            List<String> wantImages
    ) {
    }
}
