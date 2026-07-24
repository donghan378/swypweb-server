package com.swyp14.phocamatch.chat.exception;

public class InvalidChatCursorException extends RuntimeException {

    public InvalidChatCursorException() {
        super("유효하지 않은 채팅 목록 커서입니다.");
    }
}
