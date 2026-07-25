package com.swyp14.phocamatch.global.file;

import com.swyp14.phocamatch.user.exception.EmptyProfileImageException;
import com.swyp14.phocamatch.user.exception.InvalidProfileImageFormatException;
import com.swyp14.phocamatch.user.exception.ProfileImageSizeExceededException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class ProfileImageValidator {

    private static final long MAX_FILE_SIZE =
            5L * 1024L * 1024L;

    private static final Set<String>
            ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png"
            );

    public void validate(
            MultipartFile image
    ) {
        validateNotEmpty(image);
        validateSize(image);
        validateContentType(image);
        validateExtension(image);
    }

    private void validateNotEmpty(
            MultipartFile image
    ) {
        if (image == null || image.isEmpty()) {
            throw new EmptyProfileImageException();
        }
    }

    private void validateSize(
            MultipartFile image
    ) {
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new ProfileImageSizeExceededException();
        }
    }

    private void validateContentType(
            MultipartFile image
    ) {
        String contentType =
                image.getContentType();

        if (
                contentType == null
                        || !ALLOWED_CONTENT_TYPES
                        .contains(contentType)
        ) {
            throw new InvalidProfileImageFormatException();
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
            throw new InvalidProfileImageFormatException();
        }

        String extension =
                originalFilename
                        .substring(
                                originalFilename
                                        .lastIndexOf(".")
                                        + 1
                        )
                        .toLowerCase();

        if (
                !extension.equals("jpg")
                        && !extension.equals("jpeg")
                        && !extension.equals("png")
        ) {
            throw new InvalidProfileImageFormatException();
        }
    }
}
