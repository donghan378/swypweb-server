package com.swyp14.phocamatch.chat.controller;

import com.swyp14.phocamatch.chat.dto.ChatMessageHistoryResponse;
import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.exception.ChatRoomNotFoundException;
import com.swyp14.phocamatch.chat.service.ChatMessageQueryService;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
@Validated
public class ChatMessageController {

    private final ChatMessageQueryService
            chatMessageQueryService;

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?>
    getMessageHistory(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "chatId는 양수여야 합니다.")
            Long chatId,

            @RequestParam(required = false)
            @Positive(message = "cursor는 양수여야 합니다.")
            Long cursor,

            @RequestParam(defaultValue = "30")
            @Min(
                    value = 1,
                    message = "size는 1 이상이어야 합니다."
            )
            @Max(
                    value = 100,
                    message = "size는 100 이하여야 합니다."
            )
            int size
    ) {

        try{
            Long userId =
                    Long.valueOf(jwt.getSubject());

            ChatMessageHistoryResponse response =
                    chatMessageQueryService
                            .getMessageHistory(
                                    userId,
                                    chatId,
                                    cursor,
                                    size
                            );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "과거 메세지 내역 조회 완료",
                                    response
                            )
                    );
        }catch(ChatRoomNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ErrorResponse.of("RESOURCE_001",e.getMessage())
                    );
        }catch(ChatRoomAccessDeniedException e){
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ErrorResponse.of("AUTH_007",e.getMessage())
                    );
        }
    }
}
