package com.swyp14.phocamatch.global.exception;

import com.swyp14.phocamatch.chat.exception.ChatRoomAccessDeniedException;
import com.swyp14.phocamatch.chat.exception.ChatRoomNotFoundException;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.error.FieldErrorResponse;
import com.swyp14.phocamatch.tradeproposal.exception.AlreadyCompletedTradeException;
import com.swyp14.phocamatch.tradeproposal.exception.InvalidTradeCompletionCardException;
import com.swyp14.phocamatch.user.exception.DuplicateNicknameException;
import com.swyp14.phocamatch.user.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * 존재하지 않는 채팅방
     */
    @ExceptionHandler(ChatRoomNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleChatRoomNotFound(
            ChatRoomNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                "RESOURCE_001",
                                exception.getMessage()
                        )
                );
    }

    /*
     * 존재하지 않는 사용자
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleUserNotFound(
            UserNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                "RESOURCE_001",
                                exception.getMessage()
                        )
                );
    }

    /*
     * 이미 완료된 교환
     */
    @ExceptionHandler(
            AlreadyCompletedTradeException.class
    )
    public ResponseEntity<ErrorResponse>
    handleAlreadyCompletedTrade(
            AlreadyCompletedTradeException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ErrorResponse.of(
                                "RESOURCE_006",
                                exception.getMessage()
                        )
                );
    }

    /*
     * 채팅방 접근 권한 없음
     */
    @ExceptionHandler(
            ChatRoomAccessDeniedException.class
    )
    public ResponseEntity<ErrorResponse>
    handleChatRoomAccessDenied(
            ChatRoomAccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        ErrorResponse.of(
                                "AUTH_007",
                                exception.getMessage()
                        )
                );
    }

    /*
     * 닉네임 중복
     */
    @ExceptionHandler(
            DuplicateNicknameException.class
    )
    public ResponseEntity<ErrorResponse>
    handleDuplicateNickname(
            DuplicateNicknameException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ErrorResponse.of(
                                "RESOURCE_002",
                                exception.getMessage()
                        )
                );
    }

    /*
     * 교환 완료 카드 검증 오류
     */
    @ExceptionHandler(
            InvalidTradeCompletionCardException.class
    )
    public ResponseEntity<ErrorResponse>
    handleInvalidTradeCompletionCard(
            InvalidTradeCompletionCardException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorResponse.of(
                                "VALIDATION_007",
                                exception.getMessage()
                        )
                );
    }

    /*
     * 필수 Query Parameter 누락
     */
    @ExceptionHandler(
            MissingServletRequestParameterException.class
    )
    public ResponseEntity<ErrorResponse>
    handleMissingRequestParameter(
            MissingServletRequestParameterException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorResponse.of(
                                "VALIDATION_001",
                                exception.getParameterName()
                                        + " 값은 필수입니다."
                        )
                );
    }

    /*
     * 존재하지 않는 API 경로
     */
    @ExceptionHandler(
            NoResourceFoundException.class
    )
    public ResponseEntity<ErrorResponse>
    handleNoResourceFound(
            NoResourceFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                "RESOURCE_001",
                                "요청한 API를 찾을 수 없습니다."
                        )
                );
    }

    /*
     * 예상하지 못한 서버 오류
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleUnexpectedException(
            Exception exception
    ) {
        /*
         * 실제 프로젝트에서는 반드시 로그를 남긴다.
         */
        exception.printStackTrace();

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(
                        ErrorResponse.of(
                                "SERVER_001",
                                "서버 내부 오류가 발생했습니다."
                        )
                );
    }
}
