package com.swyp14.phocamatch.collection.exception;

import java.util.List;

public class InvalidPhotoCardIdsException extends RuntimeException {

    private final List<Long> invalidPhotoCardIds;

    public InvalidPhotoCardIdsException(
            List<Long> invalidPhotoCardIds
    ) {
        super(
                "존재하지 않거나 해당 그룹에 속하지 않는 포토카드가 포함되어 있습니다: "
                        + invalidPhotoCardIds
        );

        this.invalidPhotoCardIds = invalidPhotoCardIds;
    }

    public List<Long> getInvalidPhotoCardIds() {
        return invalidPhotoCardIds;
    }
}
