package com.swyp14.phocamatch.tradeset.exception;

import java.util.List;

public class InvalidTradeSetCardException extends RuntimeException {

    private final List<Long> invalidCardIds;

    public InvalidTradeSetCardException(
            List<Long> invalidCardIds
    ) {
        super(
                "존재하지 않거나 해당 그룹에 속하지 않는 포토카드가 포함되어 있습니다: "
                        + invalidCardIds
        );

        this.invalidCardIds = invalidCardIds;
    }

    public List<Long> getInvalidCardIds() {
        return invalidCardIds;
    }
}
