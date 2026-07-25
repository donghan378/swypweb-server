package com.swyp14.phocamatch.global.s3;

public class S3UploadException extends RuntimeException {

    public S3UploadException(Throwable cause) {
        super("이미지 업로드에 실패했습니다.", cause);
    }
}
