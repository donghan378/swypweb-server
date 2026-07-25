package com.swyp14.phocamatch.chat.service;

import com.swyp14.phocamatch.chat.dto.ChatImageUploadResponse;
import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.exception.ChatRoomNotFoundException;
import com.swyp14.phocamatch.chat.repository.ChatRoomMemberRepository;
import com.swyp14.phocamatch.chat.repository.ChatRoomRepository;
import com.swyp14.phocamatch.chat.validator.ChatImageValidator;
import com.swyp14.phocamatch.global.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ChatImageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository
            chatRoomMemberRepository;
    private final ChatImageValidator
            chatImageValidator;
    private final S3FileService s3FileService;

    @Transactional(readOnly = true)
    public ChatImageUploadResponse upload(
            Long userId,
            Long chatRoomId,
            MultipartFile image
    ) {
        chatImageValidator.validate(image);

        validateChatRoom(chatRoomId);

        validateParticipant(
                chatRoomId,
                userId
        );

        S3FileService.S3UploadResult uploadResult =
                s3FileService.uploadChatImage(
                        chatRoomId,
                        image
                );

        return new ChatImageUploadResponse(
                uploadResult.url()
        );
    }

    private void validateChatRoom(
            Long chatRoomId
    ) {
        if (!chatRoomRepository.existsById(chatRoomId)) {
            throw new ChatRoomNotFoundException();
        }
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
}
