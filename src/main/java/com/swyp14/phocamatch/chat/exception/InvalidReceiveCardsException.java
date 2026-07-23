package com.swyp14.phocamatch.chat.exception;

public class InvalidReceiveCardsException extends RuntimeException {

    public InvalidReceiveCardsException() {
        super("받을 카드 중 상대방의 있어요 목록에 포함되지 않은 카드가 있습니다.");
    }
}
