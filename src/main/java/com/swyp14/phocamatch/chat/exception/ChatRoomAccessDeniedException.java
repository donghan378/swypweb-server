package com.swyp14.phocamatch.chat.exception;

public class ChatRoomAccessDeniedException extends RuntimeException {

    public ChatRoomAccessDeniedException() {
        super("해당 채팅방에 접근할 권한이 없습니다.");
    }
}
