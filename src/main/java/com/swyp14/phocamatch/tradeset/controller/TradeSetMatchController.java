package com.swyp14.phocamatch.tradeset.controller;

import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.tradeset.dto.TradeSetMatchListResponse;
import com.swyp14.phocamatch.tradeset.service.TradeSetMatchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trade-sets")
@RequiredArgsConstructor
@Validated
public class TradeSetMatchController {

    private final TradeSetMatchService tradeSetMatchService;

    @GetMapping("/{tradeSetId}/matches")
    public ResponseEntity<ApiResponse<TradeSetMatchListResponse>>
    getMatches(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "tradeSetId는 양수여야 합니다.")
            Long tradeSetId,

            @RequestParam(required = false)
            String cursor,

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
        Long userId =
                Long.valueOf(jwt.getSubject());

        TradeSetMatchListResponse response =
                tradeSetMatchService.getMatches(
                        userId,
                        tradeSetId,
                        cursor,
                        size
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                200,
                                "매칭된 교환 세트 목록 조회 완료",
                                response
                        )
                );
    }
}
