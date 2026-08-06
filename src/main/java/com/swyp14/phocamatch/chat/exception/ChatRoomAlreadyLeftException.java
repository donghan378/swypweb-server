package com.swyp14.phocamatch.chat.exception;

public class ChatRoomAlreadyLeftException extends RuntimeException {

    public ChatRoomAlreadyLeftException() {
        super("이미 나간 채팅방입니다.");
    }
}
