package com.swyp14.phocamatch.auth.controller;

import com.swyp14.phocamatch.auth.dto.SignupRequest;
import com.swyp14.phocamatch.auth.dto.TokenResponse;
import com.swyp14.phocamatch.auth.exception.InvalidRefreshTokenException;
import com.swyp14.phocamatch.auth.service.AuthService;
import com.swyp14.phocamatch.auth.token.RefreshTokenCookieProvider;
import com.swyp14.phocamatch.auth.token.RefreshTokenService;
import com.swyp14.phocamatch.auth.token.TokenService;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.exception.DuplicateNicknameException;
import com.swyp14.phocamatch.user.exception.WithdrawnUserLoginException;
import com.swyp14.phocamatch.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletResponse response
    ){
        try{

            AuthService.SignupResult result =
                    authService.signup(request.signupToken(), request.nickname());

            setRefreshTokenCookie(response, result.refreshToken());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(
                                    HttpStatus.CREATED.value(),
                                    "회원가입 성공",
                                    TokenResponse.bearer(result.accessToken())
                            )
                    );
        }catch(DuplicateNicknameException exception){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of("RESOURCE_002", exception.getMessage()));
        }
    }

    /**
     * Access Token이 만료됐을 때 프론트가 호출한다.
     * 쿠키의 Refresh Token을 검증 -> 회전(재발급) -> 새 Access Token 응답.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(
                    name = RefreshTokenCookieProvider.COOKIE_NAME,
                    required = false
            )
            String refreshTokenCookie,
            HttpServletResponse response
    ) {

        try{
            if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
                throw new InvalidRefreshTokenException(
                        "Refresh Token이 없습니다. 다시 로그인해 주세요."
                );
            }

            RefreshTokenService.RotationResult rotationResult =
                    refreshTokenService.rotate(refreshTokenCookie);

            User user = userService.getUser(rotationResult.userId());
            String newAccessToken = tokenService.createAccessToken(user);

            setRefreshTokenCookie(response, rotationResult.newRawToken());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "토큰 재발급 완료",
                                     TokenResponse.bearer(newAccessToken)
                            )
                    );

        }catch(InvalidRefreshTokenException e){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of("AUTH_003", e.getMessage()));
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(
                    name = RefreshTokenCookieProvider.COOKIE_NAME,
                    required = false
            )
            String refreshTokenCookie,
            HttpServletResponse response
    ) {
        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            refreshTokenService.revoke(refreshTokenCookie);
        }

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookieProvider.clear().toString()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                200,
                                "로그아웃 완료",
                                null
                        )
                );
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String rawToken) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookieProvider.create(rawToken).toString()
        );
    }

}
