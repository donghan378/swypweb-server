package com.swyp14.phocamatch.tradeset.controller;

import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.tradeset.dto.MyTradeSetListResponse;
import com.swyp14.phocamatch.tradeset.dto.TradeSetCreateRequest;
import com.swyp14.phocamatch.tradeset.dto.TradeSetCreateResponse;
import com.swyp14.phocamatch.tradeset.exception.DuplicateTradeSetCardException;
import com.swyp14.phocamatch.tradeset.exception.InvalidTradeSetCardException;
import com.swyp14.phocamatch.tradeset.service.TradeSetService;
import jakarta.validation.Valid;
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
public class TradeSetController {
    private final TradeSetService tradeSetService;

    @PostMapping("/{groupId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?>
    createTradeSet(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "groupId는 양수여야 합니다.")
            Long groupId,

            @Valid
            @RequestBody
            TradeSetCreateRequest request
    ) {
        try{
            Long userId =
                    Long.valueOf(jwt.getSubject());

            TradeSetCreateResponse response =
                    tradeSetService.createTradeSet(
                            userId,
                            groupId,
                            request.haveCardIds(),
                            request.wantCardIds()
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "교환 세트 등록 완료",
                                    response
                            )
                    );
        }catch(DuplicateTradeSetCardException e){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ErrorResponse.of("RESOURCE_004",e.getMessage())
                    );

        }catch(InvalidTradeSetCardException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ErrorResponse.of("VALIDATION_008", e.getMessage())
                    );
        }

    }

    @GetMapping
    public ResponseEntity<?>
    getMyTradeSets(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam
            @Positive(message = "groupId는 양수여야 합니다.")
            Long groupId
    ) {
        Long userId =
                Long.valueOf(jwt.getSubject());

        MyTradeSetListResponse response =
                tradeSetService.getMyTradeSets(
                        userId,
                        groupId
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                200,
                                "교환 세트 목록 조회 성공",
                                response
                        )
                );
    }

}
