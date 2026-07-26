package com.swyp14.phocamatch.tradeproposal.service;

import com.swyp14.phocamatch.chat.domain.*;
import com.swyp14.phocamatch.chat.dto.TradeCompleteRequest;
import com.swyp14.phocamatch.chat.dto.TradeCompleteResponse;
import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.exception.ChatRoomNotFoundException;
import com.swyp14.phocamatch.chat.repository.ChatMessageRepository;
import com.swyp14.phocamatch.chat.repository.ChatRoomRepository;
import com.swyp14.phocamatch.chat.repository.TradeProposalItemRepository;
import com.swyp14.phocamatch.tradeproposal.exception.AlreadyCompletedTradeException;
import com.swyp14.phocamatch.tradeproposal.exception.InvalidTradeCompletionCardException;
import com.swyp14.phocamatch.tradeset.domain.TradeSet;
import com.swyp14.phocamatch.tradeset.domain.TradeSetItem;
import com.swyp14.phocamatch.tradeset.domain.TradeType;
import com.swyp14.phocamatch.tradeset.repository.TradeSetItemRepository;
import com.swyp14.phocamatch.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeCompletionService {

    private final ChatRoomRepository chatRoomRepository;
    private final TradeProposalItemRepository
            tradeProposalItemRepository;
    private final TradeSetItemRepository
            tradeSetItemRepository;
    private final ChatMessageRepository
            chatMessageRepository;

    @Transactional
    public TradeCompleteResult complete(
            Long userId,
            Long chatRoomId,
            TradeCompleteRequest request
    ) {
        ChatRoom chatRoom =
                chatRoomRepository
                        .findForTradeCompletion(chatRoomId)
                        .orElseThrow(
                                ChatRoomNotFoundException::new
                        );

        TradeProposal proposal =
                chatRoom.getTradeProposal();

        validateReceiver(
                proposal,
                userId
        );

        validateProposalStatus(
                proposal
        );

        validateNoDuplicateCardIds(
                request.myCardIds()
        );

        validateNoDuplicateCardIds(
                request.partnerCardIds()
        );

        List<TradeProposalItem> proposalItems =
                tradeProposalItemRepository
                        .findAllByProposalId(
                                proposal.getId()
                        );

        /*
         * 프론트에서 선택한 카드가 실제 제안에
         * 포함된 카드인지 검증한다.
         */
        validateSelectedCards(
                proposalItems,
                request
        );

        /*
         * 실제 교환된 카드로 기록한다.
         */
        markSelectedProposalItems(
                proposalItems,
                request
        );

        /*
         * 사용자가 삭제를 선택했을 때만
         * B의 대상 TradeSet에서 선택한 카드들을 삭제한다.
         */
        if (Boolean.TRUE.equals(
                request.deleteSelectedCards()
        )) {
            deleteSelectedCards(
                    proposal,
                    request
            );
        }

        proposal.complete();

        ChatMessage systemMessage =
                ChatMessage.createSystem(
                        chatRoom,
                        proposal.getReceiver(),
                        "교환이 완료되었습니다."
                );

        ChatMessage savedSystemMessage =
                chatMessageRepository.save(
                        systemMessage
                );

        return new TradeCompleteResult(
                new TradeCompleteResponse(
                        chatRoomId,
                        true,
                        request.deleteSelectedCards()
                ),
                savedSystemMessage
        );
    }

    private void validateSelectedCards(
            List<TradeProposalItem> proposalItems,
            TradeCompleteRequest request
    ) {
        Set<Long> allowedMyCardIds =
                proposalItems.stream()
                        .filter(item ->
                                item.getProposalType()
                                        == ProposalCardType.PROPOSER_RECEIVES
                        )
                        .map(item ->
                                item.getCard().getId()
                        )
                        .collect(Collectors.toSet());

        Set<Long> allowedPartnerCardIds =
                proposalItems.stream()
                        .filter(item ->
                                item.getProposalType()
                                        == ProposalCardType.PROPOSER_GIVES
                        )
                        .map(item ->
                                item.getCard().getId()
                        )
                        .collect(Collectors.toSet());

        if (!allowedMyCardIds.containsAll(
                request.myCardIds()
        )) {
            throw new InvalidTradeCompletionCardException(
                    "선택한 내 포토카드가 교환 제안에 포함되어 있지 않습니다."
            );
        }

        if (!allowedPartnerCardIds.containsAll(
                request.partnerCardIds()
        )) {
            throw new InvalidTradeCompletionCardException(
                    "선택한 상대방 포토카드가 교환 제안에 포함되어 있지 않습니다."
            );
        }
    }

    private void validateNoDuplicateCardIds(
            List<Long> cardIds
    ) {
        Set<Long> distinctIds =
                new HashSet<>(cardIds);

        if (distinctIds.size() != cardIds.size()) {
            throw new InvalidTradeCompletionCardException(
                    "동일한 포토카드를 중복 선택할 수 없습니다."
            );
        }
    }

    private void markSelectedProposalItems(
            List<TradeProposalItem> proposalItems,
            TradeCompleteRequest request
    ) {
        Set<Long> selectedMyCardIds =
                new HashSet<>(
                        request.myCardIds()
                );

        Set<Long> selectedPartnerCardIds =
                new HashSet<>(
                        request.partnerCardIds()
                );

        for (TradeProposalItem item : proposalItems) {
            Long cardId =
                    item.getCard().getId();

            boolean selectedMyCard =
                    item.getProposalType()
                            == ProposalCardType.PROPOSER_RECEIVES
                            && selectedMyCardIds.contains(cardId);

            boolean selectedPartnerCard =
                    item.getProposalType()
                            == ProposalCardType.PROPOSER_GIVES
                            && selectedPartnerCardIds.contains(cardId);
        }
    }

    private void deleteSelectedCards(
            TradeProposal proposal,
            TradeCompleteRequest request
    ) {
        TradeSet targetTradeSet =
                proposal.getTargetTradeSet();

        /*
         * B의 myCardIds:
         * B가 주는 카드이므로 대상 TradeSet의 HAVE
         */
        List<TradeSetItem> selectedHaveItems =
                tradeSetItemRepository.findSelectedItems(
                        targetTradeSet.getId(),
                        TradeType.HAVE,
                        request.myCardIds()
                );

        /*
         * B의 partnerCardIds:
         * B가 받는 카드이므로 대상 TradeSet의 WANT
         */
        List<TradeSetItem> selectedWantItems =
                tradeSetItemRepository.findSelectedItems(
                        targetTradeSet.getId(),
                        TradeType.WANT,
                        request.partnerCardIds()
                );

        /*
         * 조회된 항목 개수까지 검사하면
         * 대상 교환 세트에 없는 카드 삭제 요청을 막을 수 있다.
         */
        if (
                selectedHaveItems.size()
                        != request.myCardIds().size()
        ) {
            throw new InvalidTradeCompletionCardException(
                    "선택한 내 포토카드가 대상 교환 세트에 없습니다."
            );
        }

        if (
                selectedWantItems.size()
                        != request.partnerCardIds().size()
        ) {
            throw new InvalidTradeCompletionCardException(
                    "선택한 상대방 포토카드가 대상 교환 세트에 없습니다."
            );
        }

        List<TradeSetItem> itemsToDelete =
                new ArrayList<>();

        itemsToDelete.addAll(selectedHaveItems);
        itemsToDelete.addAll(selectedWantItems);

        tradeSetItemRepository.deleteAll(
                itemsToDelete
        );

        tradeSetItemRepository.flush();

        deactivateIfIncomplete(
                targetTradeSet
        );
    }

    private void validateReceiver(
            TradeProposal proposal,
            Long userId
    ) {
        if (!proposal.getReceiver()
                .getId()
                .equals(userId)) {
            throw new ChatRoomAccessDeniedException();
        }
    }

    private void validateProposalStatus(
            TradeProposal proposal
    ) {
        if (proposal.isCompleted()) {
            throw new AlreadyCompletedTradeException();
        }
    }

    private void deleteTradedCards(
            TradeProposal proposal
    ) {
        List<TradeProposalItem> proposalItems =
                tradeProposalItemRepository
                        .findAllByProposalId(
                                proposal.getId()
                        );

        List<Long> tradedCardIds =
                proposalItems.stream()
                        .map(item ->
                                item.getCard().getId()
                        )
                        .distinct()
                        .toList();

        if (tradedCardIds.isEmpty()) {
            return;
        }

        TradeSet targetTradeSet =
                proposal.getTargetTradeSet();

        List<TradeSetItem> tradeSetItems =
                tradeSetItemRepository
                        .findTradedItems(
                                targetTradeSet.getId(),
                                tradedCardIds
                        );

        tradeSetItemRepository.deleteAll(
                tradeSetItems
        );

        tradeSetItemRepository.flush();

        deactivateIfIncomplete(
                targetTradeSet
        );
    }

    private void deactivateIfIncomplete(
            TradeSet targetTradeSet
    ) {
        long haveCount =
                tradeSetItemRepository
                        .countByTradeSet_IdAndTradeType(
                                targetTradeSet.getId(),
                                TradeType.HAVE
                        );

        long wantCount =
                tradeSetItemRepository
                        .countByTradeSet_IdAndTradeType(
                                targetTradeSet.getId(),
                                TradeType.WANT
                        );

        if (haveCount == 0 || wantCount == 0) {
            targetTradeSet.delete();
        }
    }

    public record TradeCompleteResult(
            TradeCompleteResponse response,
            ChatMessage systemMessage
    ) {
    }
}
