package com.swyp14.phocamatch.chat.exception;

public class EmptyChatImageException extends RuntimeException {

    public EmptyChatImageException() {
        super("업로드할 이미지 파일이 비어 있습니다.");
    }
}
