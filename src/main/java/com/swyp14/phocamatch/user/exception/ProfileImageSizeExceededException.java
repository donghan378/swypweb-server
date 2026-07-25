package com.swyp14.phocamatch.user.exception;

public class ProfileImageSizeExceededException extends RuntimeException {

    public ProfileImageSizeExceededException() {
        super("프로필 이미지는 5MB 이하만 업로드할 수 있습니다.");
    }
}
