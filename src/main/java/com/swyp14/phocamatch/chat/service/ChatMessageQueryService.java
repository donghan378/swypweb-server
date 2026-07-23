package com.swyp14.phocamatch.chat.service;

import com.swyp14.phocamatch.chat.domain.ChatMessage;
import com.swyp14.phocamatch.chat.dto.ChatMessageHistoryResponse;
import com.swyp14.phocamatch.chat.dto.ChatMessageResponse;
import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.exception.ChatRoomNotFoundException;
import com.swyp14.phocamatch.chat.repository.ChatMessageRepository;
import com.swyp14.phocamatch.chat.repository.ChatRoomMemberRepository;
import com.swyp14.phocamatch.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public ChatMessageHistoryResponse getMessageHistory(
            Long userId,
            Long chatRoomId,
            Long cursor,
            int size
    ) {
        validateChatRoomAccess(
                userId,
                chatRoomId
        );

        PageRequest pageable =
                PageRequest.of(
                        0,
                        size + 1
                );

        List<ChatMessage> queryResults;

        if (cursor == null) {
            queryResults =
                    chatMessageRepository
                            .findByChatRoom_IdOrderByIdDesc(
                                    chatRoomId,
                                    pageable
                            );
        } else {
            queryResults =
                    chatMessageRepository
                            .findByChatRoom_IdAndIdLessThanOrderByIdDesc(
                                    chatRoomId,
                                    cursor,
                                    pageable
                            );
        }

        boolean hasNext =
                queryResults.size() > size;

        List<ChatMessage> pageMessages =
                hasNext
                        ? new ArrayList<>(
                        queryResults.subList(0, size)
                )
                        : new ArrayList<>(queryResults);

        /*
         * DB에서는 최신 메시지부터 조회하지만,
         * 응답은 화면 표시가 편하도록 과거 → 최신 순서로 뒤집는다.
         */
        Collections.reverse(pageMessages);

        List<ChatMessageResponse> messages =
                pageMessages.stream()
                        .map(this::toResponse)
                        .toList();

        Long nextCursor = null;

        if (hasNext && !pageMessages.isEmpty()) {
            /*
             * 응답을 오름차순으로 뒤집었으므로
             * 첫 번째 메시지가 현재 페이지에서 가장 오래된 메시지다.
             */
            nextCursor =
                    pageMessages.get(0)
                            .getId();
        }

        return new ChatMessageHistoryResponse(
                messages,
                nextCursor,
                hasNext
        );
    }

    private void validateChatRoomAccess(
            Long userId,
            Long chatRoomId
    ) {
        if (!chatRoomRepository.existsById(chatRoomId)) {
            throw new ChatRoomNotFoundException();
        }

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

    private ChatMessageResponse toResponse(
            ChatMessage message
    ) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getMessageType(),
                message.getContent(),
                message.getImageUrl(),
                message.getCreatedAt()
        );
    }
}
