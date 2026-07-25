package com.swyp14.phocamatch.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.public-base-url:}")
    private String publicBaseUrl;

    public S3UploadResult uploadProfileImage(
            Long userId,
            MultipartFile image
    ) {
        String extension =
                extractExtension(
                        image.getOriginalFilename()
                );

        String key =
                "profile-images/"
                        + userId
                        + "/"
                        + UUID.randomUUID()
                        + "."
                        + extension;

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(
                                image.getContentType()
                        )
                        .contentLength(
                                image.getSize()
                        )
                        .build();

        try {
            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(
                            image.getBytes()
                    )
            );

            return new S3UploadResult(
                    key,
                    createFileUrl(key)
            );

        } catch (
                IOException
                | S3Exception exception
        ) {
            throw new S3UploadException(
                    exception
            );
        }
    }

    public void deleteByUrl(
            String fileUrl
    ) {
        if (
                fileUrl == null
                        || fileUrl.isBlank()
        ) {
            return;
        }

        String key = extractKey(fileUrl);

        if (key == null || key.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build();

            s3Client.deleteObject(request);

        } catch (S3Exception exception) {
            /*
             * 기존 이미지 삭제 실패 때문에
             * 프로필 수정 전체를 실패시킬지 정책 선택 가능.
             *
             * 여기서는 삭제 실패를 무시하고
             * 로그만 남기는 방식을 권장한다.
             */
        }
    }

    private String createFileUrl(
            String key
    ) {
        if (
                publicBaseUrl != null
                        && !publicBaseUrl.isBlank()
        ) {
            return removeTrailingSlash(
                    publicBaseUrl
            ) + "/" + key;
        }

        return "https://"
                + bucket
                + ".s3."
                + region
                + ".amazonaws.com/"
                + key;
    }

    private String extractKey(
            String fileUrl
    ) {
        String baseUrl;

        if (
                publicBaseUrl != null
                        && !publicBaseUrl.isBlank()
        ) {
            baseUrl =
                    removeTrailingSlash(
                            publicBaseUrl
                    ) + "/";
        } else {
            baseUrl =
                    "https://"
                            + bucket
                            + ".s3."
                            + region
                            + ".amazonaws.com/";
        }

        if (!fileUrl.startsWith(baseUrl)) {
            return null;
        }

        return fileUrl.substring(
                baseUrl.length()
        );
    }

    private String extractExtension(
            String originalFilename
    ) {
        if (
                originalFilename == null
                        || !originalFilename.contains(".")
        ) {
            throw new IllegalArgumentException(
                    "파일 확장자가 없습니다."
            );
        }

        String extension =
                originalFilename
                        .substring(
                                originalFilename
                                        .lastIndexOf(".")
                                        + 1
                        )
                        .toLowerCase();

        if (extension.equals("jpeg")) {
            return "jpg";
        }

        return extension;
    }

    private String removeTrailingSlash(
            String value
    ) {
        if (value.endsWith("/")) {
            return value.substring(
                    0,
                    value.length() - 1
            );
        }

        return value;
    }

    public record S3UploadResult(
            String key,
            String url
    ) {
    }

    public S3UploadResult uploadChatImage(
            Long chatRoomId,
            MultipartFile image
    ) {
        String extension =
                extractExtension(
                        image.getOriginalFilename()
                );

        String key =
                "chat-images/"
                        + chatRoomId
                        + "/"
                        + UUID.randomUUID()
                        + "."
                        + extension;

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(
                                image.getContentType()
                        )
                        .contentLength(
                                image.getSize()
                        )
                        .build();

        try {
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            image.getInputStream(),
                            image.getSize()
                    )
            );

            return new S3UploadResult(
                    key,
                    createFileUrl(key)
            );

        } catch (IOException | S3Exception exception) {
            throw new S3UploadException(exception);
        }
    }

    public boolean isManagedUrl(
            String fileUrl
    ) {
        if (
                fileUrl == null
                        || fileUrl.isBlank()
        ) {
            return false;
        }

        String baseUrl;

        if (
                publicBaseUrl != null
                        && !publicBaseUrl.isBlank()
        ) {
            baseUrl =
                    removeTrailingSlash(
                            publicBaseUrl
                    ) + "/chat-images/";
        } else {
            baseUrl =
                    "https://"
                            + bucket
                            + ".s3."
                            + region
                            + ".amazonaws.com/chat-images/";
        }

        return fileUrl.startsWith(baseUrl);
    }
}
