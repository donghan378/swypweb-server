package com.swyp14.phocamatch.favoritegroup.exception;

import java.util.List;

public class InvalidGroupIdsException extends RuntimeException {

    private final List<Long> invalidGroupIds;

    public InvalidGroupIdsException(
            List<Long> invalidGroupIds
    ){
        super(
                "존재하지 않는 그룹 ID가 포함되어 있습니다." + invalidGroupIds
        );

        this.invalidGroupIds = invalidGroupIds;
    }

    public List<Long> getInvalidGroupIds() {
        return invalidGroupIds;
    }
}
