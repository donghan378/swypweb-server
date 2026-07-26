package com.swyp14.phocamatch.tradeproposal.exception;

public class AlreadyCompletedTradeException extends RuntimeException {

    public AlreadyCompletedTradeException() {
        super("이미 완료 처리된 채팅입니다.");
    }
}
