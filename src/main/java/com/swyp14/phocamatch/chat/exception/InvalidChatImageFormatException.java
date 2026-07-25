package com.swyp14.phocamatch.chat.exception;

public class InvalidChatImageFormatException extends RuntimeException {
    public InvalidChatImageFormatException() {
        super("jpg 또는 png 형식의 이미지만 업로드할 수 있습니다.");
    }
}
