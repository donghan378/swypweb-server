package com.swyp14.phocamatch.user.exception;

public class InvalidProfileImageFormatException extends RuntimeException {

    public InvalidProfileImageFormatException() {
        super("jpg 또는 png 형식의 이미지만 업로드할 수 있습니다.");
    }
}
