package com.swyp14.phocamatch.auth.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieProvider {

    public static final String COOKIE_NAME = "refresh_token";

    @Value("${app.jwt.refresh-token-expiration-seconds}")
    private long refreshTokenExpirationSeconds;

    // 로컬(http)에서는 false, 운영 배포(https)에서는 true로 설정
    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    // 프론트/백엔드가 서로 다른 도메인이면 None, 같은 사이트(localhost 등)면 Lax
    @Value("${app.cookie.same-site}")
    private String sameSite;

    public ResponseCookie create(String rawToken) {
        return ResponseCookie.from(COOKIE_NAME, rawToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(refreshTokenExpirationSeconds)
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}
