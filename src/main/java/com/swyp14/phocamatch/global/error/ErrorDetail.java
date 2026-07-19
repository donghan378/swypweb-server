package com.swyp14.phocamatch.global.error;

import java.util.List;

public record ErrorDetail(
        String code,
        String message,
        List<FieldErrorResponse> fieldErrors
) {
}
