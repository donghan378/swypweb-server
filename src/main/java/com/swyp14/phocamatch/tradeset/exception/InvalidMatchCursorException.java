package com.swyp14.phocamatch.tradeset.exception;

public class InvalidMatchCursorException extends RuntimeException {

    public InvalidMatchCursorException() {
        super("유효하지 않은 매칭 커서입니다.");
    }
}
