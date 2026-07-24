package com.swyp14.phocamatch.legal.exception;

public class LegalDocumentNotFoundException extends RuntimeException {

    public LegalDocumentNotFoundException() {
        super("문서를 찾을 수 없습니다.");
    }
}
