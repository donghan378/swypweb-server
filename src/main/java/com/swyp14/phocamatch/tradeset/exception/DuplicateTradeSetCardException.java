package com.swyp14.phocamatch.tradeset.exception;

import java.util.List;

public class DuplicateTradeSetCardException extends RuntimeException {

    private final List<Long> duplicatedCardIds;

    public DuplicateTradeSetCardException(
            List<Long> duplicatedCardIds
    ) {
        super(
                "같은 포토카드를 있어요와 원해요에 동시에 등록할 수 없습니다: "
                        + duplicatedCardIds
        );

        this.duplicatedCardIds = duplicatedCardIds;
    }

    public List<Long> getDuplicatedCardIds() {
        return duplicatedCardIds;
    }
}
