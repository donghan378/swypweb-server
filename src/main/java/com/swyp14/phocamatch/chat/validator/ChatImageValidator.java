package com.swyp14.phocamatch.chat.validator;

import com.swyp14.phocamatch.chat.exception.ChatImageSizeExceededException;
import com.swyp14.phocamatch.chat.exception.EmptyChatImageException;
import com.swyp14.phocamatch.chat.exception.InvalidChatImageFormatException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
public class ChatImageValidator {
    private static final long MAX_FILE_SIZE =
            10L * 1024L * 1024L;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png"
            );

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png"
            );

    public void validate(MultipartFile image) {
        validateNotEmpty(image);
        validateSize(image);
        validateContentType(image);
        validateExtension(image);
    }

    private void validateNotEmpty(
            MultipartFile image
    ) {
        if (image == null || image.isEmpty()) {
            throw new EmptyChatImageException();
        }
    }

    private void validateSize(
            MultipartFile image
    ) {
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new ChatImageSizeExceededException();
        }
    }

    private void validateContentType(
            MultipartFile image
    ) {
        String contentType =
                image.getContentType();

        if (
                contentType == null
                        || !ALLOWED_CONTENT_TYPES.contains(contentType)
        ) {
            throw new InvalidChatImageFormatException();
        }
    }

    private void validateExtension(
            MultipartFile image
    ) {
        String originalFilename =
                image.getOriginalFilename();

        if (
                originalFilename == null
                        || !originalFilename.contains(".")
        ) {
            throw new InvalidChatImageFormatException();
        }

        String extension =
                originalFilename
                        .substring(
                                originalFilename.lastIndexOf(".") + 1
                        )
                        .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidChatImageFormatException();
        }
    }
}
