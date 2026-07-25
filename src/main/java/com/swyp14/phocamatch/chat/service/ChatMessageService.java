package com.swyp14.phocamatch.chat.service;

import com.swyp14.phocamatch.chat.domain.ChatMessage;
import com.swyp14.phocamatch.chat.domain.ChatRoom;
import com.swyp14.phocamatch.chat.domain.MessageType;
import com.swyp14.phocamatch.chat.dto.ChatMessageResponse;
import com.swyp14.phocamatch.chat.dto.ChatMessageSendRequest;
import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.exception.ChatRoomNotFoundException;
import com.swyp14.phocamatch.chat.exception.InvalidChatMessageException;
import com.swyp14.phocamatch.chat.repository.ChatMessageRepository;
import com.swyp14.phocamatch.chat.repository.ChatRoomMemberRepository;
import com.swyp14.phocamatch.chat.repository.ChatRoomRepository;
import com.swyp14.phocamatch.global.s3.S3FileService;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.exception.UserNotFoundException;
import com.swyp14.phocamatch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository
            chatRoomMemberRepository;
    private final ChatMessageRepository
            chatMessageRepository;
    private final UserRepository userRepository;

    private final S3FileService s3FileService;

    @Transactional
    public ChatMessageResponse sendMessage(
            Long userId,
            ChatMessageSendRequest request
    ) {
        ChatRoom chatRoom =
                chatRoomRepository
                        .findById(request.chatRoomId())
                        .orElseThrow(
                                ChatRoomNotFoundException::new
                        );

        validateParticipant(
                request.chatRoomId(),
                userId
        );

        User sender =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                UserNotFoundException::new
                        );

        ChatMessage message =
                createMessage(
                        chatRoom,
                        sender,
                        request
                );

        ChatMessage savedMessage =
                chatMessageRepository.save(message);

        /*
         * @CreationTimestamp 및 ID 값이 즉시 필요하므로
         * INSERT를 확실하게 실행한다.
         */
        chatMessageRepository.flush();

        return new ChatMessageResponse(
                savedMessage.getId(),
                sender.getId(),
                savedMessage.getMessageType(),
                savedMessage.getContent(),
                savedMessage.getImageUrl(),
                savedMessage.getCreatedAt()
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

    private ChatMessage createMessage(
            ChatRoom chatRoom,
            User sender,
            ChatMessageSendRequest request
    ) {
        if (request.type() == MessageType.TEXT) {
            validateTextMessage(request);

            return ChatMessage.createText(
                    chatRoom,
                    sender,
                    request.content().trim()
            );
        }

        if (request.type() == MessageType.IMAGE) {
            validateImageMessage(request);

            return ChatMessage.createImage(
                    chatRoom,
                    sender,
                    request.imageUrl()
            );
        }

        throw new InvalidChatMessageException(
                "사용자가 전송할 수 없는 메시지 타입입니다."
        );
    }

    private void validateTextMessage(
            ChatMessageSendRequest request
    ) {
        if (
                request.content() == null
                        || request.content().isBlank()
        ) {
            throw new InvalidChatMessageException(
                    "텍스트 메시지 내용은 필수입니다."
            );
        }

        if (request.content().length() > 2000) {
            throw new InvalidChatMessageException(
                    "텍스트 메시지는 2000자 이하여야 합니다."
            );
        }

        if (
                request.imageUrl() != null
                        && !request.imageUrl().isBlank()
        ) {
            throw new InvalidChatMessageException(
                    "텍스트 메시지에는 imageUrl을 포함할 수 없습니다."
            );
        }
    }

    private void validateImageMessage(
            ChatMessageSendRequest request
    ) {
        if (
                request.imageUrl() == null
                        || request.imageUrl().isBlank()
        ) {
            throw new InvalidChatMessageException(
                    "이미지 메시지의 imageUrl은 필수입니다."
            );
        }

        if (!s3FileService.isManagedUrl(
                request.imageUrl()
        )) {
            throw new InvalidChatMessageException(
                    "허용되지 않은 이미지 URL입니다."
            );
        }

        if (
                request.content() != null
                        && !request.content().isBlank()
        ) {
            throw new InvalidChatMessageException(
                    "이미지 메시지에는 content를 포함할 수 없습니다."
            );
        }
    }
}
