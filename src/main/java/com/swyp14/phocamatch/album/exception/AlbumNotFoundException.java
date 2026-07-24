package com.swyp14.phocamatch.album.exception;

public class AlbumNotFoundException extends RuntimeException {

    public AlbumNotFoundException() {
        super("존재하지 않는 앨범입니다.");
    }
}
