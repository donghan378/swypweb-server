package com.swyp14.phocamatch.tradeset.support;

import com.swyp14.phocamatch.tradeset.dto.TradeSetMatchCursor;
import com.swyp14.phocamatch.tradeset.exception.InvalidMatchCursorException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
public class TradeSetMatchCursorCodec {

    public String encode(
            Long matchScore,
            LocalDateTime createdAt,
            Long tradeSetId
    ) {
        String rawCursor =
                matchScore
                        + "|"
                        + createdAt
                        + "|"
                        + tradeSetId;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        rawCursor.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    public TradeSetMatchCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new TradeSetMatchCursor(
                    null,
                    null,
                    null
            );
        }

        try {
            String decoded =
                    new String(
                            Base64.getUrlDecoder()
                                    .decode(cursor),
                            StandardCharsets.UTF_8
                    );

            String[] values =
                    decoded.split("\\|", -1);

            if (values.length != 3) {
                throw new IllegalArgumentException();
            }

            return new TradeSetMatchCursor(
                    Long.valueOf(values[0]),
                    LocalDateTime.parse(values[1]),
                    Long.valueOf(values[2])
            );
        } catch (RuntimeException exception) {
            throw new InvalidMatchCursorException();
        }
    }
}
