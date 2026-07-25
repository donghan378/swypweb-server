package com.swyp14.phocamatch.user.controller;

import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.global.s3.S3UploadException;
import com.swyp14.phocamatch.user.dto.ProfileImageUpdateResponse;
import com.swyp14.phocamatch.user.exception.EmptyProfileImageException;
import com.swyp14.phocamatch.user.exception.InvalidProfileImageFormatException;
import com.swyp14.phocamatch.user.exception.ProfileImageSizeExceededException;
import com.swyp14.phocamatch.user.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ProfileImageController {

    private final ProfileImageService
            profileImageService;

    @PatchMapping(
            value = "/me/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?>
    updateProfileImage(
            @AuthenticationPrincipal Jwt jwt,

            @RequestPart("image")
            MultipartFile image
    ) {

        try{
            Long userId =
                    Long.valueOf(jwt.getSubject());

            ProfileImageUpdateResponse response =
                    profileImageService.update(
                            userId,
                            image
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "프로필 이미지 수정 완료",
                                    response
                            )
                    );
        }catch(InvalidProfileImageFormatException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of("VALIDATION_004",e.getMessage())
                    );
        }catch(ProfileImageSizeExceededException e){
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(
                            ErrorResponse.of("VALIDATION_005",e.getMessage())
                    );
        }catch(MaxUploadSizeExceededException e){
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(
                            ErrorResponse.of("VALIDATION_005","프로필 이미지는 5MB 이하만 업로드할 수 있습니다.")
                    );
        }catch(EmptyProfileImageException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of("VALIDATION_001",e.getMessage())
                    );
        }catch(S3UploadException e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ErrorResponse.of("SERVER_002","프로필 이미지 업로드에 실패했습니다.")
                    );
        }
    }
}
