package com.swyp14.phocamatch.tradeset.exception;

public class TradeSetNotFoundException extends RuntimeException {
    public TradeSetNotFoundException() {
        super("존재하지 않는 교환 세트입니다.");
    }
}
