package com.swyp14.phocamatch.tradeproposal.controller;


import com.swyp14.phocamatch.chat.dto.ChatMessageResponse;
import com.swyp14.phocamatch.chat.dto.TradeCompleteRequest;
import com.swyp14.phocamatch.chat.dto.TradeCompleteResponse;
import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.exception.ChatRoomNotFoundException;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.tradeproposal.exception.AlreadyCompletedTradeException;
import com.swyp14.phocamatch.tradeproposal.exception.InvalidTradeCompletionCardException;
import com.swyp14.phocamatch.tradeproposal.service.TradeCompletionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
@Validated
public class TradeCompletionController {

    private final TradeCompletionService
            tradeCompletionService;
    private final SimpMessagingTemplate
            messagingTemplate;

    @PatchMapping("/{chatId}/complete")
    public ResponseEntity<?>
    completeTrade(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "chatId는 양수여야 합니다.")
            Long chatId,

            @Valid
            @RequestBody
            TradeCompleteRequest request
    ) {

        try{
            Long userId =
                    Long.valueOf(jwt.getSubject());

            TradeCompletionService.TradeCompleteResult result =
                    tradeCompletionService.complete(
                            userId,
                            chatId,
                            request
                    );

            ChatMessageResponse systemMessageResponse =
                    ChatMessageResponse.from(
                            result.systemMessage()
                    );

            messagingTemplate.convertAndSend(
                    "/sub/chat/rooms/" + chatId,
                    systemMessageResponse
            );

            messagingTemplate.convertAndSend(
                    "/sub/chat/rooms/"
                            + chatId
                            + "/completion",
                    result.response()
            );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "교환 완료 처리 성공",
                                    result.response()
                            )
                    );
        }catch(AlreadyCompletedTradeException  e){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ErrorResponse.of("RESOURCE_006", e.getMessage())
                    );
        }catch(ChatRoomAccessDeniedException e){
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ErrorResponse.of("AUTH_007", "교환은 제안받은 사용자만 완료할 수 있습니다.")
                    );
        }catch(InvalidTradeCompletionCardException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of("VALIDATION_007", e.getMessage())
                    );
        }catch(ChatRoomNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ErrorResponse.of("RESOURCE_001", e.getMessage())
                    );
        }
    }
}
