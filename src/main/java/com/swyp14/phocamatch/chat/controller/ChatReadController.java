package com.swyp14.phocamatch.chat.controller;

import com.swyp14.phocamatch.chat.dto.ChatReadResponse;
import com.swyp14.phocamatch.chat.service.ChatReadService;
import com.swyp14.phocamatch.global.response.ApiResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
@Validated
public class ChatReadController {

    private final ChatReadService chatReadService;

    @PatchMapping("/{chatId}/read")
    public ResponseEntity<ApiResponse<ChatReadResponse>>
    markChatRoomAsRead(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "chatId는 양수여야 합니다.")
            Long chatId
    ) {
        Long userId =
                Long.valueOf(jwt.getSubject());

        ChatReadResponse response =
                chatReadService.markChatRoomAsRead(
                        userId,
                        chatId
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                200,
                                "메세지 읽음 처리 완료",
                                response
                        )
                );
    }
}
