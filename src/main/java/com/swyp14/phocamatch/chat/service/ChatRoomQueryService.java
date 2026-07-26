package com.swyp14.phocamatch.chat.service;

import com.swyp14.phocamatch.chat.domain.ChatRoom;
import com.swyp14.phocamatch.chat.domain.ProposalCardType;
import com.swyp14.phocamatch.chat.domain.TradeProposal;
import com.swyp14.phocamatch.chat.domain.TradeProposalStatus;
import com.swyp14.phocamatch.chat.dto.*;
import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.exception.ChatRoomNotFoundException;
import com.swyp14.phocamatch.chat.repository.ChatRoomMemberRepository;
import com.swyp14.phocamatch.chat.repository.ChatRoomRepository;
import com.swyp14.phocamatch.chat.repository.TradeProposalItemRepository;
import com.swyp14.phocamatch.chat.repository.TradeProposalRepository;
import com.swyp14.phocamatch.chat.support.ChatRoomCursorCodec;
import com.swyp14.phocamatch.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private final ChatRoomMemberRepository
            chatRoomMemberRepository;

    private final ChatRoomCursorCodec
            cursorCodec;

    private final ChatRoomRepository chatRoomRepository;
    private final TradeProposalItemRepository tradeProposalItemRepository;

    @Transactional(readOnly = true)
    public ChatRoomListResponse getMyChatRooms(
            Long userId,
            String cursor,
            int size
    ) {
        ChatRoomCursor decodedCursor =
                cursorCodec.decode(cursor);

        List<ChatRoomListProjection> queryResults =
                chatRoomMemberRepository.findChatRooms(
                        userId,
                        decodedCursor.lastMessageAt(),
                        decodedCursor.chatRoomId(),
                        size + 1
                );

        boolean hasNext =
                queryResults.size() > size;

        List<ChatRoomListProjection> pageResults =
                hasNext
                        ? queryResults.subList(0, size)
                        : queryResults;

        List<ChatRoomListItemResponse> chats =
                pageResults.stream()
                        .map(this::toResponse)
                        .toList();

        String nextCursor = null;

        if (hasNext && !pageResults.isEmpty()) {
            ChatRoomListProjection last =
                    pageResults.get(
                            pageResults.size() - 1
                    );

            nextCursor =
                    cursorCodec.encode(
                            last.getLastMessageAt(),
                            last.getChatId()
                    );
        }

        return new ChatRoomListResponse(
                chats,
                nextCursor,
                hasNext
        );
    }

    private ChatRoomListItemResponse toResponse(
            ChatRoomListProjection result
    ) {
        return new ChatRoomListItemResponse(
                result.getChatId(),
                result.getPartnerNickname(),
                result.getPartnerProfileImageUrl(),
                resolveLastMessage(
                        result.getLastMessageType(),
                        result.getLastMessageContent()
                ),
                result.getLastMessageAt(),
                result.getUnreadCount(),
                TradeProposalStatus.COMPLETED
                        .name()
                        .equals(
                                result.getProposalStatus()
                        )
        );
    }

    private String resolveLastMessage(
            String messageType,
            String content
    ) {
        if (messageType == null) {
            return "교환 제안이 도착했습니다.";
        }

        if ("IMAGE".equals(messageType)) {
            return "[이미지]";
        }

        return content;
    }

    @Transactional(readOnly = true)
    public ChatRoomHeaderResponse getChatRoomHeader(
            Long userId,
            Long chatRoomId
    ) {
        ChatRoom chatRoom = chatRoomRepository
                .findHeaderById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        validateParticipant(
                chatRoomId,
                userId
        );

        TradeProposal proposal =
                chatRoom.getTradeProposal();

        boolean isProposer =
                proposal.getProposer()
                        .getId()
                        .equals(userId);

        User partner =
                isProposer
                        ? proposal.getReceiver()
                        : proposal.getProposer();

        List<TradeProposalCardProjection> cards =
                tradeProposalItemRepository
                        .findCardsByProposalId(
                                proposal.getId()
                        );

        ProposalCardType myHaveType =
                isProposer
                        ? ProposalCardType.PROPOSER_GIVES
                        : ProposalCardType.PROPOSER_RECEIVES;

        ProposalCardType myWantType =
                isProposer
                        ? ProposalCardType.PROPOSER_RECEIVES
                        : ProposalCardType.PROPOSER_GIVES;

        List<TradeProposalCardProjection> haveCards =
                cards.stream()
                        .filter(card ->
                                card.getProposalType()
                                        == myHaveType
                        )
                        .toList();

        List<TradeProposalCardProjection> wantCards =
                cards.stream()
                        .filter(card ->
                                card.getProposalType()
                                        == myWantType
                        )
                        .toList();

        ChatRepresentativeCardResponse representHaveCard =
                haveCards.isEmpty()
                        ? null
                        : toRepresentativeCard(
                        haveCards.get(0)
                );

        ChatRepresentativeCardResponse representWantCard =
                wantCards.isEmpty()
                        ? null
                        : toRepresentativeCard(
                        wantCards.get(0)
                );

        boolean completed =
                proposal.getStatus()
                        == TradeProposalStatus.COMPLETED;

        boolean isReceiver = proposal.isReceiver(userId);

        return new ChatRoomHeaderResponse(
                chatRoom.getId(),
                partner.getNickname(),
                completed,
                isReceiver,
                representHaveCard,
                representWantCard,
                haveCards.size(),
                wantCards.size()
        );
    }

    private void validateParticipant(
            Long chatRoomId,
            Long userId
    ) {
        boolean participant =
                chatRoomMemberRepository
                        .existsByChatRoom_IdAndUser_Id(
                                chatRoomId,
                                userId
                        );

        if (!participant) {
            throw new ChatRoomAccessDeniedException();
        }
    }

    private ChatRepresentativeCardResponse
    toRepresentativeCard(
            TradeProposalCardProjection card
    ) {
        return new ChatRepresentativeCardResponse(
                card.getPhotoCardName(),
                card.getAlbumName(),
                card.getVersionName(),
                card.getImageUrl()
        );
    }

    @Transactional(readOnly = true)
    public TradeProposalDetailResponse getTradeProposalDetail(
            Long userId,
            Long chatRoomId
    ) {
        ChatRoom chatRoom = chatRoomRepository
                .findWithProposalById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        validateParticipant(
                chatRoomId,
                userId
        );

        TradeProposal proposal =
                chatRoom.getTradeProposal();

        boolean isProposer =
                proposal.getProposer()
                        .getId()
                        .equals(userId);

        ProposalCardType myHaveType =
                isProposer
                        ? ProposalCardType.PROPOSER_GIVES
                        : ProposalCardType.PROPOSER_RECEIVES;

        ProposalCardType myWantType =
                isProposer
                        ? ProposalCardType.PROPOSER_RECEIVES
                        : ProposalCardType.PROPOSER_GIVES;

        List<TradeProposalCardProjection> cardResults =
                tradeProposalItemRepository
                        .findCardsByProposalId(
                                proposal.getId()
                        );

        List<TradeProposalCardResponse> haveCards =
                cardResults.stream()
                        .filter(card ->
                                card.getProposalType()
                                        == myHaveType
                        )
                        .map(this::toCardResponse)
                        .toList();

        List<TradeProposalCardResponse> wantCards =
                cardResults.stream()
                        .filter(card ->
                                card.getProposalType()
                                        == myWantType
                        )
                        .map(this::toCardResponse)
                        .toList();

        return new TradeProposalDetailResponse(
                haveCards,
                wantCards
        );
    }

    private TradeProposalCardResponse toCardResponse(
            TradeProposalCardProjection card
    ) {
        return new TradeProposalCardResponse(
                card.getPhotoCardId(),
                card.getPhotoCardName(),
                card.getAlbumName(),
                card.getVersionName(),
                card.getImageUrl()
        );
    }
}
