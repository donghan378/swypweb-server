package com.swyp14.phocamatch.idolgroup.controller;

import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.idolgroup.dto.IdolGroupListResponse;
import com.swyp14.phocamatch.idolgroup.service.IdolGroupService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/groups")
@RequiredArgsConstructor
@Validated
public class IdolGroupController {

    private final IdolGroupService idolGroupService;

    @GetMapping
    public ResponseEntity<ApiResponse<IdolGroupListResponse>> getAllGroups(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(required = false)
            @Positive(message = "cursor는 양수여야 합니다.")
            Long cursor,

            @RequestParam(defaultValue = "10")
            @Min(
                    value = 1,
                    message = "size는 1 이상이어야 합니다."
            )
            @Max(
                    value = 50,
                    message = "size는 50 이하여야 합니다."
            )
            int size
    ) {
        Long userId = parseUserId(jwt);

        IdolGroupListResponse response =
                idolGroupService.getAllGroups(
                        userId,
                        cursor,
                        size
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                200,
                                "그룹 목록 조회 성공",
                                response
                        )
                );
    }

    private Long parseUserId(Jwt jwt) {

        if(jwt == null) {
            return null; // 비로그인 요청 -> userId 없음
        }

        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Access Token의 사용자 ID가 올바르지 않습니다."
            );
        }
    }
}
