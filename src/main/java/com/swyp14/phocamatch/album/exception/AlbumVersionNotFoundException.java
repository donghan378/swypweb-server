package com.swyp14.phocamatch.album.exception;

public class AlbumVersionNotFoundException extends RuntimeException {

    public AlbumVersionNotFoundException() {
        super("존재하지 않는 앨범 버전입니다.");
    }
}
