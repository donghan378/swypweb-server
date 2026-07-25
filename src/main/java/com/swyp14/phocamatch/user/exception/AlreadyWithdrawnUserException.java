package com.swyp14.phocamatch.user.exception;

public class AlreadyWithdrawnUserException extends RuntimeException {

    public AlreadyWithdrawnUserException() {
        super("이미 탈퇴 처리된 계정입니다.");
    }
}
