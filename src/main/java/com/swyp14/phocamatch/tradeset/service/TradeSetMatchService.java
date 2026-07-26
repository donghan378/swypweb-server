package com.swyp14.phocamatch.tradeset.service;

import com.swyp14.phocamatch.tradeset.domain.TradeSet;
import com.swyp14.phocamatch.tradeset.domain.TradeSetStatus;
import com.swyp14.phocamatch.tradeset.dto.*;
import com.swyp14.phocamatch.tradeset.exception.TradeSetAccessDeniedException;
import com.swyp14.phocamatch.tradeset.exception.TradeSetNotFoundException;
import com.swyp14.phocamatch.tradeset.repository.TradeSetItemRepository;
import com.swyp14.phocamatch.tradeset.repository.TradeSetRepository;
import com.swyp14.phocamatch.tradeset.support.TradeSetMatchCursorCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TradeSetMatchService {

    private final TradeSetRepository tradeSetRepository;
    private final TradeSetItemRepository tradeSetItemRepository;
    private final TradeSetMatchCursorCodec cursorCodec;

    @Transactional(readOnly = true)
    public TradeSetMatchListResponse getMatches(
            Long userId,
            Long tradeSetId,
            String cursor,
            int size
    ) {
        TradeSet myTradeSet =
                tradeSetRepository.findById(tradeSetId)
                        .orElseThrow(
                                TradeSetNotFoundException::new
                        );

        if (myTradeSet.getStatus()
                != TradeSetStatus.ACTIVE) {
            throw new TradeSetNotFoundException();
        }

        validateOwner(
                myTradeSet,
                userId
        );

        TradeSetMatchCursor decodedCursor =
                cursorCodec.decode(cursor);

        List<TradeSetMatchCandidateProjection> candidates =
                tradeSetRepository.findMatchCandidates(
                        tradeSetId,
                        myTradeSet.getGroup().getId(),
                        userId,
                        decodedCursor.matchScore(),
                        decodedCursor.createdAt(),
                        decodedCursor.tradeSetId(),
                        size + 1
                );

        boolean hasNext =
                candidates.size() > size;

        List<TradeSetMatchCandidateProjection> pageCandidates =
                hasNext
                        ? candidates.subList(0, size)
                        : candidates;

        if (pageCandidates.isEmpty()) {
            return new TradeSetMatchListResponse(
                    List.of(),
                    null,
                    false
            );
        }

        List<Long> candidateIds =
                pageCandidates.stream()
                        .map(
                                TradeSetMatchCandidateProjection
                                        ::getTradeSetId
                        )
                        .toList();

        List<MatchedCardProjection> matchedCardResults =
                tradeSetItemRepository.findMatchedCards(
                        tradeSetId,
                        candidateIds
                );

        Map<Long, List<MatchedPhotoCardResponse>>
                matchedHaveCardMap =
                new LinkedHashMap<>();

        Map<Long, List<MatchedPhotoCardResponse>>
                matchedWantCardMap =
                new LinkedHashMap<>();

        for (MatchedCardProjection result
                : matchedCardResults) {

            MatchedPhotoCardResponse cardResponse =
                    new MatchedPhotoCardResponse(
                            result.getPhotoCardId(),
                            result.getImageUrl()
                    );

            if ("HAVE".equals(result.getTradeType())) {
                matchedHaveCardMap
                        .computeIfAbsent(
                                result.getTradeSetId(),
                                ignored -> new ArrayList<>()
                        )
                        .add(cardResponse);
            } else {
                matchedWantCardMap
                        .computeIfAbsent(
                                result.getTradeSetId(),
                                ignored -> new ArrayList<>()
                        )
                        .add(cardResponse);
            }
        }

        List<TradeSetMatchResponse> matches =
                pageCandidates.stream()
                        .map(candidate ->
                                new TradeSetMatchResponse(
                                        candidate.getTradeSetId(),
                                        candidate.getUserId(),
                                        candidate.getNickname(),
                                        candidate.getProfileImageUrl(),
                                        candidate.getMatchScore(),
                                        matchedHaveCardMap
                                                .getOrDefault(
                                                        candidate
                                                                .getTradeSetId(),
                                                        List.of()
                                                ),
                                        matchedWantCardMap
                                                .getOrDefault(
                                                        candidate
                                                                .getTradeSetId(),
                                                        List.of()
                                                )
                                )
                        )
                        .toList();

        String nextCursor = null;

        if (hasNext) {
            TradeSetMatchCandidateProjection last =
                    pageCandidates.get(
                            pageCandidates.size() - 1
                    );

            nextCursor =
                    cursorCodec.encode(
                            last.getMatchScore(),
                            last.getCreatedAt(),
                            last.getTradeSetId()
                    );
        }

        return new TradeSetMatchListResponse(
                matches,
                nextCursor,
                hasNext
        );
    }

    private void validateOwner(
            TradeSet tradeSet,
            Long userId
    ) {
        if (!tradeSet.getUser().getId().equals(userId)) {
            throw new TradeSetAccessDeniedException();
        }
    }
}
