package com.swyp14.phocamatch.idolgroup.exception;

public class IdolGroupNotFoundException extends RuntimeException{

    public IdolGroupNotFoundException(){
        super("존재하지 않는 그룹입니다.");
    }
}
