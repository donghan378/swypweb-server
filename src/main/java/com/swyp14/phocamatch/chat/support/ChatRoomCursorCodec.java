package com.swyp14.phocamatch.chat.support;

import com.swyp14.phocamatch.chat.dto.ChatRoomCursor;
import com.swyp14.phocamatch.chat.exception.InvalidChatCursorException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
public class ChatRoomCursorCodec {
    public String encode(
            LocalDateTime lastMessageAt,
            Long chatRoomId
    ) {
        String raw =
                lastMessageAt
                        + "|"
                        + chatRoomId;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        raw.getBytes(StandardCharsets.UTF_8)
                );
    }

    public ChatRoomCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new ChatRoomCursor(
                    null,
                    null
            );
        }

        try {
            String decoded =
                    new String(
                            Base64.getUrlDecoder().decode(cursor),
                            StandardCharsets.UTF_8
                    );

            String[] values =
                    decoded.split("\\|", -1);

            if (values.length != 2) {
                throw new IllegalArgumentException();
            }

            return new ChatRoomCursor(
                    LocalDateTime.parse(values[0]),
                    Long.valueOf(values[1])
            );
        } catch (RuntimeException exception) {
            throw new InvalidChatCursorException();
        }
    }

}
