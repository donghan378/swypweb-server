package com.swyp14.phocamatch.favoritegroup.exception;

public class FavoriteGroupNotFoundException extends  RuntimeException{

    public FavoriteGroupNotFoundException() {
        super("관심 등록된 그룹을 찾을 수 없습니다.");
    }
}
