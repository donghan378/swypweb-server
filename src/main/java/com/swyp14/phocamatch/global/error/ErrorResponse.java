package com.swyp14.phocamatch.global.error;

import java.util.List;

public record ErrorResponse(
        boolean success,
        Object data,
        ErrorDetail error
) {

    public static ErrorResponse of(
            String code,
            String message
    ){
        return new ErrorResponse(
                false,
                null,
                new ErrorDetail(
                        code,
                        message,
                        List.of()
                )
        );
    }

    public static ErrorResponse of(
            String code,
            String message,
            List<FieldErrorResponse> fieldErrors
    ){
        return new ErrorResponse(
                false,
                null,
                new ErrorDetail(
                        code,
                        message,
                        fieldErrors
                )
        );
    }
}


