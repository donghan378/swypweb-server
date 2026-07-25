package com.swyp14.phocamatch.chat.controller;

import com.swyp14.phocamatch.chat.dto.ChatImageUploadResponse;
import com.swyp14.phocamatch.chat.exception.ChatImageSizeExceededException;
import com.swyp14.phocamatch.chat.exception.EmptyChatImageException;
import com.swyp14.phocamatch.chat.exception.InvalidChatImageFormatException;
import com.swyp14.phocamatch.chat.service.ChatImageService;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
@Validated
public class ChatImageController {

    private final ChatImageService chatImageService;

    @PostMapping(
            value = "/{chatId}/messages/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?>
    uploadChatImage(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "chatId는 양수여야 합니다.")
            Long chatId,

            @RequestPart("image")
            MultipartFile image
    ) {
        try{
            Long userId =
                    Long.valueOf(jwt.getSubject());

            ChatImageUploadResponse response =
                    chatImageService.upload(
                            userId,
                            chatId,
                            image
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "이미지 첨부 성공",
                                    response
                            )
                    );
        }catch(InvalidChatImageFormatException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of(
                                    "VALIDATION_004",e.getMessage()
                            )
                    );
        }catch(ChatImageSizeExceededException e){
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(
                            ErrorResponse.of(
                                    "VALIDATION_005",e.getMessage()
                            )
                    );
        }catch(MaxUploadSizeExceededException e){
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(
                            ErrorResponse.of(
                                "VALIDATION_005","채팅 이미지는 10MB 이하만 업로드할 수 있습니다."
                            )
                    );
        }catch(EmptyChatImageException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of(
                                "VALIDATION_001",e.getMessage()
                            )
                    );
        }/*catch(MissingServletRequestPartException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of(
                                    "VALIDATION_001","이미지 파일은 필수입니다."
                            )
                    );
        }*/
    }
}
