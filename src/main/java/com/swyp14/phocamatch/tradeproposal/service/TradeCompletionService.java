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
import com.swyp14.phocamatch.tradeset.domain.TradeSet;
import com.swyp14.phocamatch.tradeset.domain.TradeSetItem;
import com.swyp14.phocamatch.tradeset.domain.TradeType;
import com.swyp14.phocamatch.tradeset.repository.TradeSetItemRepository;
import com.swyp14.phocamatch.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        validateReceiver(proposal, userId);
        validateProposalStatus(proposal);

        if (Boolean.TRUE.equals(
                request.deleteTradedCards()
        )) {
            deleteTradedCards(proposal);
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

        chatMessageRepository.flush();

        TradeCompleteResponse response =
                new TradeCompleteResponse(
                        chatRoomId,
                        true,
                        request.deleteTradedCards()
                );

        return new TradeCompleteResult(
                response,
                savedSystemMessage
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
