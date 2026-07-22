package com.swyp14.phocamatch.tradeset.exception;

public class TradeSetAccessDeniedException extends RuntimeException {
    public TradeSetAccessDeniedException() {
        super("본인이 등록한 교환 세트만 수정할 수 있습니다.");
    }
}
