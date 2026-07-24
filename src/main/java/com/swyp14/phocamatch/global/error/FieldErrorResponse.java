package com.swyp14.phocamatch.global.error;

// Validation 오류가 발생했을때 어떤 필드가 잘못됐는지 표현하는 객체
public record FieldErrorResponse(
        String field,
        Object rejectedValue,
        String message
) {
}
