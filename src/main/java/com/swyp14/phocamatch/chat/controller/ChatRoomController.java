package com.swyp14.phocamatch.chat.controller;

import com.swyp14.phocamatch.chat.dto.ChatRoomCreateRequest;
import com.swyp14.phocamatch.chat.dto.ChatRoomCreateResponse;
import com.swyp14.phocamatch.chat.dto.ChatRoomListResponse;
import com.swyp14.phocamatch.chat.exception.InvalidGiveCardsException;
import com.swyp14.phocamatch.chat.exception.InvalidReceiveCardsException;
import com.swyp14.phocamatch.chat.exception.SelfTradeProposalException;
import com.swyp14.phocamatch.chat.service.ChatRoomQueryService;
import com.swyp14.phocamatch.chat.service.ChatRoomService;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomQueryService chatRoomQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?>
    createChatRoom(
            @AuthenticationPrincipal Jwt jwt,

            @Valid
            @RequestBody
            ChatRoomCreateRequest request
    ) {

        try{
            Long userId =
                    Long.valueOf(jwt.getSubject());

            ChatRoomCreateResponse response =
                    chatRoomService.createChatRoom(
                            userId,
                            request
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "채팅 생성 완료",
                                    response
                            )
                    );
        }catch(InvalidReceiveCardsException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of("VALIDATION_009",e.getMessage())
                    );
        }catch(InvalidGiveCardsException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of("VALIDATION_010",e.getMessage())
                    );
        }catch(SelfTradeProposalException e){
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ErrorResponse.of("AUTH_006",e.getMessage())
                    );
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ChatRoomListResponse>>
    getMyChatRooms(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(required = false)
            String cursor,

            @RequestParam(defaultValue = "10")
            @Min(
                    value = 1,
                    message = "size는 1 이상이어야 합니다."
            )
            @Max(
                    value = 50,
                    message = "size는 50 이하여야 합니다."
            )
            int size
    ) {
        Long userId =
                Long.valueOf(jwt.getSubject());

        ChatRoomListResponse response =
                chatRoomQueryService.getMyChatRooms(
                        userId,
                        cursor,
                        size
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                200,
                                "채팅 목록 조회 완료",
                                response
                        )
                );
    }
}
