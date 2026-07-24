package com.swyp14.phocamatch.global.response;

public record ApiResponse<T>(
        boolean success,
        int code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(
            int code,
            String message,
            T data
    ) {
        return new ApiResponse<>(
                true,
                code,
                message,
                data
        );
    }

    public static ApiResponse<Void> success(
            int code,
            String message
    ){
        return new ApiResponse<>(
                true,
                code,
                message,
                null
        );
    }
}
