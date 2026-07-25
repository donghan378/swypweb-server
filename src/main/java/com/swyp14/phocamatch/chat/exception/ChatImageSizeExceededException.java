package com.swyp14.phocamatch.chat.exception;

public class ChatImageSizeExceededException extends RuntimeException{

    public ChatImageSizeExceededException() {
        super("채팅 이미지는 10MB 이하만 업로드할 수 있습니다.");
    }
}
