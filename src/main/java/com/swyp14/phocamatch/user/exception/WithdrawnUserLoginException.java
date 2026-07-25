package com.swyp14.phocamatch.user.exception;

public class WithdrawnUserLoginException extends RuntimeException {

    public WithdrawnUserLoginException() {
        super("탈퇴한 계정은 로그인할 수 없습니다.");
    }
}
