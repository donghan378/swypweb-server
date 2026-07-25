package com.swyp14.phocamatch.tradeset.controller;

import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.idolgroup.exception.IdolGroupNotFoundException;
import com.swyp14.phocamatch.tradeset.dto.TradeFeedResponse;
import com.swyp14.phocamatch.tradeset.service.TradeFeedService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Validated
public class TradeFeedController {

    private final TradeFeedService tradeFeedService;

    @GetMapping("/trade-sets")
    public ResponseEntity<?> getFeed(
            @RequestParam(required = false)
            @Positive(message = "groupId는 양수여야 합니다.")
            Long groupId,

            @RequestParam(required = false)
            @Positive(message = "cursor는 양수여야 합니다.")
            Long cursor,

            @RequestParam(
                    required = false,
                    defaultValue = "10"
            )
            @Min(
                    value = 1,
                    message = "size는 1 이상이어야 합니다."
            )
            @Max(
                    value = 100,
                    message = "size는 100 이하여야 합니다."
            )
            Integer size
    ) {
        try{
            TradeFeedResponse response =
                    tradeFeedService.getFeed(
                            groupId,
                            cursor,
                            size
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "홈 피드 조회 완료",
                                    response
                            )
                    );
        }catch(IdolGroupNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of("RESOURCE_001",e.getMessage())
                    );
        }
    }
}
