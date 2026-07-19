package com.swyp14.phocamatch.legal.controller;

import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.legal.dto.LegalDocumentResponse;
import com.swyp14.phocamatch.legal.exception.LegalDocumentNotFoundException;
import com.swyp14.phocamatch.legal.service.LegalDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;

    @GetMapping("/service")
    public ResponseEntity<?>
    getTemrsOfService(){

        try{
            LegalDocumentResponse response =
                    legalDocumentService
                            .getTermsOfService();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "약관 조회 성공",
                                    response
                            )
                    );
        } catch(LegalDocumentNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("404",e.getMessage()));
        }

    }

    @GetMapping("/privacy")
    public ResponseEntity<?>
    getPrivacyPolicy(){

        try{
            LegalDocumentResponse response =
                    legalDocumentService
                            .getPrivacyPolicy();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "개인정보 처리방침 조회 성공",
                                    response
                            )
                    );
        } catch(LegalDocumentNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("404",e.getMessage()));
        }

    }
}
