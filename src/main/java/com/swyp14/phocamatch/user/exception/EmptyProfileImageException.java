package com.swyp14.phocamatch.user.exception;

public class EmptyProfileImageException extends RuntimeException {

    public EmptyProfileImageException() {
        super("업로드할 이미지 파일이 비어 있습니다.");
    }
}
