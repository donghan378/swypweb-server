package com.swyp14.phocamatch.user.service;

import com.swyp14.phocamatch.global.file.ProfileImageValidator;
import com.swyp14.phocamatch.global.s3.S3FileService;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.dto.ProfileImageUpdateResponse;
import com.swyp14.phocamatch.user.exception.UserNotFoundException;
import com.swyp14.phocamatch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final UserRepository userRepository;
    private final ProfileImageValidator
            profileImageValidator;
    private final S3FileService s3FileService;

    @Transactional
    public ProfileImageUpdateResponse update(
            Long userId,
            MultipartFile image
    ) {
        profileImageValidator.validate(image);

        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        UserNotFoundException::new
                );

        String previousImageUrl =
                user.getProfileImageUrl();

        S3FileService.S3UploadResult uploadResult =
                s3FileService.uploadProfileImage(
                        userId,
                        image
                );

        try {
            user.updateProfileImageUrl(
                    uploadResult.url()
            );

            /*
             * DB UPDATE 오류를 이 메서드 안에서
             * 즉시 확인하기 위한 flush.
             */
            userRepository.flush();

        } catch (RuntimeException exception) {
            /*
             * DB 반영 실패 시 새로 업로드한
             * 사용되지 않는 파일을 제거한다.
             */
            s3FileService.deleteByUrl(
                    uploadResult.url()
            );

            throw exception;
        }

        /*
         * 새 이미지와 DB 변경이 성공한 뒤
         * 기존 이미지를 제거한다.
         */
        if (
                previousImageUrl != null
                        && !previousImageUrl.isBlank()
                        && !previousImageUrl.equals(
                        uploadResult.url()
                )
        ) {
            s3FileService.deleteByUrl(
                    previousImageUrl
            );
        }

        return new ProfileImageUpdateResponse(
                uploadResult.url()
        );
    }
}
