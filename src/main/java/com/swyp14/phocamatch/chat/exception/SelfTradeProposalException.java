package com.swyp14.phocamatch.chat.exception;

public class SelfTradeProposalException extends RuntimeException {

    public SelfTradeProposalException() {
        super("본인의 교환 세트에는 제안할 수 없습니다.");
    }
}
