package com.swyp14.phocamatch.chat.exception;

public class InvalidGiveCardsException extends RuntimeException {

    public InvalidGiveCardsException() {
        super("줄 카드 중 상대방의 원해요 목록에 포함되지 않은 카드가 있습니다.");
    }
}
