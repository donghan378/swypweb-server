package com.swyp14.phocamatch.chat.service;

import com.swyp14.phocamatch.chat.domain.ChatRoomMember;
import com.swyp14.phocamatch.chat.dto.ChatReadResponse;
import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.exception.ChatRoomNotFoundException;
import com.swyp14.phocamatch.chat.repository.ChatMessageRepository;
import com.swyp14.phocamatch.chat.repository.ChatRoomMemberRepository;
import com.swyp14.phocamatch.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatReadResponse markChatRoomAsRead(
            Long userId,
            Long chatRoomId
    ) {
        if (!chatRoomRepository.existsById(chatRoomId)) {
            throw new ChatRoomNotFoundException();
        }

        ChatRoomMember member =
                chatRoomMemberRepository
                        .findByChatRoom_IdAndUser_Id(
                                chatRoomId,
                                userId
                        )
                        .orElseThrow(
                                ChatRoomAccessDeniedException::new
                        );

        Long latestMessageId =
                chatMessageRepository.findLatestMessageId(
                        chatRoomId
                );

        member.updateLastReadMessageId(
                latestMessageId
        );

        long unreadCount =
                calculateUnreadCount(
                        chatRoomId,
                        userId,
                        member.getLastReadMessageId()
                );

        return new ChatReadResponse(
                chatRoomId,
                unreadCount
        );
    }

    private long calculateUnreadCount(
            Long chatRoomId,
            Long userId,
            Long lastReadMessageId
    ) {
        if (lastReadMessageId == null) {
            return 0L;
        }

        return chatMessageRepository
                .countUnreadMessages(
                        chatRoomId,
                        userId,
                        lastReadMessageId
                );
    }
}
