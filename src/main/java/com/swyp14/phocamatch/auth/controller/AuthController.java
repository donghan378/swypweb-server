package com.swyp14.phocamatch.auth.controller;

import com.swyp14.phocamatch.auth.dto.SignupRequest;
import com.swyp14.phocamatch.auth.dto.TokenResponse;
import com.swyp14.phocamatch.auth.service.AuthService;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.user.exception.DuplicateNicknameException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request
    ){
        try{
            TokenResponse tokenResponse = authService.signup(
                    request.signupToken(),
                    request.nickname()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(
                                    HttpStatus.CREATED.value(),
                                    "회원가입 성공",
                                    tokenResponse
                            )
                    );
        }catch(DuplicateNicknameException exception){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of("RESOURCE_002", exception.getMessage()));
        }
    }
}
