package com.swyp14.phocamatch.chat.service;

import com.swyp14.phocamatch.chat.domain.*;
import com.swyp14.phocamatch.chat.dto.ChatRoomCreateRequest;
import com.swyp14.phocamatch.chat.dto.ChatRoomCreateResponse;
import com.swyp14.phocamatch.chat.dto.ChatRoomDeleteResponse;
import com.swyp14.phocamatch.chat.exception.*;
import com.swyp14.phocamatch.chat.repository.*;
import com.swyp14.phocamatch.photocard.domain.PhotoCard;
import com.swyp14.phocamatch.photocard.repository.PhotoCardRepository;
import com.swyp14.phocamatch.tradeset.domain.TradeSet;
import com.swyp14.phocamatch.tradeset.domain.TradeSetStatus;
import com.swyp14.phocamatch.tradeset.domain.TradeType;
import com.swyp14.phocamatch.tradeset.exception.TradeSetNotFoundException;
import com.swyp14.phocamatch.tradeset.repository.TradeSetItemRepository;
import com.swyp14.phocamatch.tradeset.repository.TradeSetRepository;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.exception.UserNotFoundException;
import com.swyp14.phocamatch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final UserRepository userRepository;
    private final TradeSetRepository tradeSetRepository;
    private final TradeSetItemRepository tradeSetItemRepository;
    private final PhotoCardRepository photoCardRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final TradeProposalRepository tradeProposalRepository;
    private final TradeProposalItemRepository tradeProposalItemRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public ChatRoomCreateResponse createChatRoom(
            Long userId,
            ChatRoomCreateRequest request
    ) {
        User proposer = userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);

        TradeSet targetTradeSet = tradeSetRepository
                .findById(request.targetTradeSetId())
                .filter(tradeSet ->
                        tradeSet.getStatus()
                                == TradeSetStatus.ACTIVE
                )
                .orElseThrow(TradeSetNotFoundException::new);

        User receiver =
                targetTradeSet.getUser();

        validateNotSelfProposal(
                proposer,
                receiver
        );

        List<Long> receiveCardIds =
                removeDuplicates(
                        request.receiveCardIds()
                );

        List<Long> giveCardIds =
                removeDuplicates(
                        request.giveCardIds()
                );

        validateReceiveCards(
                targetTradeSet.getId(),
                receiveCardIds
        );

        validateGiveCards(
                targetTradeSet.getId(),
                giveCardIds
        );

        List<Long> allCardIds =
                mergeCardIds(
                        receiveCardIds,
                        giveCardIds
                );

        List<PhotoCard> photoCards =
                photoCardRepository.findAllById(
                        allCardIds
                );

        validateAllCardsExist(
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

        TradeProposal proposal =
                tradeProposalRepository.save(
                        TradeProposal.create(
                                targetTradeSet,
                                proposer,
                                receiver
                        )
                );

        List<TradeProposalItem> proposalItems =
                new ArrayList<>();

        giveCardIds.forEach(cardId ->
                proposalItems.add(
                        TradeProposalItem.create(
                                proposal,
                                photoCardMap.get(cardId),
                                ProposalCardType.PROPOSER_GIVES
                        )
                )
        );

        receiveCardIds.forEach(cardId ->
                proposalItems.add(
                        TradeProposalItem.create(
                                proposal,
                                photoCardMap.get(cardId),
                                ProposalCardType.PROPOSER_RECEIVES
                        )
                )
        );

        tradeProposalItemRepository.saveAll(
                proposalItems
        );

        ChatRoom chatRoom =
                chatRoomRepository.save(
                        ChatRoom.create(proposal)
                );

        chatRoomMemberRepository.saveAll(
                List.of(
                        ChatRoomMember.create(
                                chatRoom,
                                proposer
                        ),
                        ChatRoomMember.create(
                                chatRoom,
                                receiver
                        )
                )
        );

        return new ChatRoomCreateResponse(
                chatRoom.getId(),
                proposal.getId(),
                receiver.getNickname(),
                receiver.getProfileImageUrl()
        );
    }

    private void validateNotSelfProposal(
            User proposer,
            User receiver
    ) {
        if (proposer.getId().equals(receiver.getId())) {
            throw new SelfTradeProposalException();
        }
    }

    private void validateReceiveCards(
            Long targetTradeSetId,
            List<Long> receiveCardIds
    ) {
        Set<Long> targetHaveCardIds =
                new HashSet<>(
                        tradeSetItemRepository
                                .findCardIdsByTradeSetIdAndType(
                                        targetTradeSetId,
                                        TradeType.HAVE
                                )
                );

        boolean invalid =
                receiveCardIds.stream()
                        .anyMatch(cardId ->
                                !targetHaveCardIds.contains(cardId)
                        );

        if (invalid) {
            throw new InvalidReceiveCardsException();
        }
    }

    private void validateGiveCards(
            Long targetTradeSetId,
            List<Long> giveCardIds
    ) {
        Set<Long> targetWantCardIds =
                new HashSet<>(
                        tradeSetItemRepository
                                .findCardIdsByTradeSetIdAndType(
                                        targetTradeSetId,
                                        TradeType.WANT
                                )
                );

        boolean invalid =
                giveCardIds.stream()
                        .anyMatch(cardId ->
                                !targetWantCardIds.contains(cardId)
                        );

        if (invalid) {
            throw new InvalidGiveCardsException();
        }
    }

    private List<Long> removeDuplicates(
            List<Long> cardIds
    ) {
        return List.copyOf(
                new LinkedHashSet<>(cardIds)
        );
    }

    private List<Long> mergeCardIds(
            List<Long> first,
            List<Long> second
    ) {
        LinkedHashSet<Long> merged =
                new LinkedHashSet<>();

        merged.addAll(first);
        merged.addAll(second);

        return List.copyOf(merged);
    }

    private void validateAllCardsExist(
            List<Long> requestedCardIds,
            List<PhotoCard> foundCards
    ) {
        Set<Long> foundIds =
                foundCards.stream()
                        .map(PhotoCard::getId)
                        .collect(Collectors.toSet());

        boolean invalid =
                requestedCardIds.stream()
                        .anyMatch(cardId ->
                                !foundIds.contains(cardId)
                        );

        if (invalid) {
            throw new IllegalArgumentException(
                    "존재하지 않는 포토카드가 포함되어 있습니다."
            );
        }
    }

    @Transactional
    public ChatRoomDeleteResponse leaveChatRoom(Long chatRoomId, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException());

        ChatRoomMember member = chatRoomMemberRepository
                .findByChatRoomIdAndUserId(chatRoom.getId(), userId)
                .orElseThrow(() -> new ChatRoomAccessDeniedException());

        if (member.isDeleted()) {
            throw new ChatRoomAlreadyLeftException();
        }

        // 1. 요청한 유저만 나가기(soft delete) 처리
        member.leave(LocalDateTime.now());

        // 2. 채팅방의 전체 멤버가 모두 나갔는지 확인
        List<ChatRoomMember> allMembers = chatRoomMemberRepository
                .findAllByChatRoomId(chatRoom.getId());

        boolean allMembersLeft = allMembers.stream().allMatch(ChatRoomMember::isDeleted);

        // 3. 전원 나간 경우 -> 실제 데이터 삭제 (메시지 -> 멤버 -> 방 순서, FK 제약 주의)
        if (allMembersLeft) {
            chatMessageRepository.deleteAllByChatRoomId(chatRoom.getId());
            chatRoomMemberRepository.deleteAllByChatRoomId(chatRoom.getId());
            chatRoomRepository.delete(chatRoom);
        }

        return ChatRoomDeleteResponse.of(chatRoom.getId(), allMembersLeft);
    }

}
