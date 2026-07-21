package com.swyp14.phocamatch.collection.controller;

import com.swyp14.phocamatch.collection.dto.CollectionBatchSaveRequest;
import com.swyp14.phocamatch.collection.dto.CollectionBatchSaveResponse;
import com.swyp14.phocamatch.collection.exception.InvalidPhotoCardIdsException;
import com.swyp14.phocamatch.collection.service.CollectionService;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/collections")
@RequiredArgsConstructor
@Validated
public class CollectionController {
    private final CollectionService collectionService;

    @PutMapping("/groups/{groupId}/photocards")
    public ResponseEntity<?>
    saveGroupCollection(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "groupId는 양수여야 합니다.")
            Long groupId,

            @Valid
            @RequestBody
            CollectionBatchSaveRequest request
    ) {
        try{
            Long userId = parseUserId(jwt);

            CollectionBatchSaveResponse response =
                    collectionService.saveGroupCollection(
                            userId,
                            groupId,
                            request.photoCardIds()
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "포토카드 보유 목록 저장 완료",
                                    response
                            )
                    );
        }catch( InvalidPhotoCardIdsException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of("VALIDATION_007",e.getMessage())
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
