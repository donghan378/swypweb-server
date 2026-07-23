package com.swyp14.phocamatch.chat.exception;

public class ChatRoomNotFoundException extends RuntimeException {

    public ChatRoomNotFoundException() {
        super("존재하지 않는 채팅방입ㄴ다.");
    }
}
