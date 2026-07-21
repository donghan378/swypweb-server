package com.swyp14.phocamatch.photocard.controller;

import com.swyp14.phocamatch.album.exception.AlbumVersionNotFoundException;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.photocard.dto.PhotoCardListResponse;
import com.swyp14.phocamatch.photocard.service.PhotoCardService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/collections")
@RequiredArgsConstructor
@Validated
public class PhotoCardController {

    private final PhotoCardService photoCardService;

    @GetMapping("/versions/{versionId}/photocards")
    public ResponseEntity<?>
    getPhotoCardsByVersion(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "versionId는 양수여야 합니다.")
            Long versionId
    ) {

        try{
            Long userId = parseUserId(jwt);

            PhotoCardListResponse response =
                    photoCardService.getPhotoCardsByVersion(
                            userId,
                            versionId
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "포토카드 목록 조회 성공",
                                    response
                            )
                    );
        }catch(AlbumVersionNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ErrorResponse.of("RESOURCE_001",e.getMessage())
                    );
        }
    }

    private Long parseUserId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (
                NullPointerException
                | NumberFormatException exception
        ) {
            throw new IllegalArgumentException(
                    "Access Token의 사용자 ID가 올바르지 않습니다."
            );
        }
    }
}
