package com.swyp14.phocamatch.chat.service;

import com.swyp14.phocamatch.chat.domain.TradeProposalStatus;
import com.swyp14.phocamatch.chat.dto.ChatRoomCursor;
import com.swyp14.phocamatch.chat.dto.ChatRoomListItemResponse;
import com.swyp14.phocamatch.chat.dto.ChatRoomListProjection;
import com.swyp14.phocamatch.chat.dto.ChatRoomListResponse;
import com.swyp14.phocamatch.chat.repository.ChatRoomMemberRepository;
import com.swyp14.phocamatch.chat.support.ChatRoomCursorCodec;
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
}
