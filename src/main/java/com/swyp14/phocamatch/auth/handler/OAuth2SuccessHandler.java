package com.swyp14.phocamatch.auth.handler;

import com.swyp14.phocamatch.auth.token.TokenService;
import com.swyp14.phocamatch.user.domain.AuthProvider;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.exception.WithdrawnUserLoginException;
import com.swyp14.phocamatch.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Value("${app.frontend.login-success-url}")
    private String frontendSuccessUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String providerUserId = oidcUser.getSubject();

        String email = oidcUser.getEmail();

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalStateException(
                    "Google 사용자 식별자를 가져올 수 없습니다."
            );
        }

        if(email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "Google 이메일을 가져올 수 없습니다."
            );
        }

        Optional<User> optionalUser =
                userRepository
                        .findByProviderAndProviderUserId(
                                AuthProvider.GOOGLE,
                                providerUserId
                        );

        String redirectUrl;

        if(optionalUser.isPresent()) {
            User user = optionalUser.get();

            if(user.isWithdrawn()){
                redirectToWithdrawnUserPage(response);
                return;
            }

            if(!email.equals(user.getEmail())) {
                user.updateEmail(email);
            }

            String accessToken =
                    tokenService.createAccessToken(user);

            redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendSuccessUrl)
                    .fragment(
                            "status=LOGIN_SUCCESS"
                                + "&access_token="
                                + accessToken
                    )
                    .build()
                    .toUriString();
        } else {
            String signupToken =
                    tokenService.createSignupToken(
                            AuthProvider.GOOGLE,
                            providerUserId,
                            email
                    );

            redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendSuccessUrl)
                    .fragment(
                            "status=SIGNUP_REQUIRED"
                                + "&signup_token="
                                + signupToken
                    )
                    .build()
                    .toUriString();
        }

        HttpSession session = request.getSession(false);

        if(session != null) {
            session.invalidate();
        }

        response.sendRedirect(redirectUrl);

    }

    private void redirectToWithdrawnUserPage(
            HttpServletResponse response
    ) throws IOException {

        String redirectUrl =
                UriComponentsBuilder
                        .fromUriString(
                                "https://swypweb-client.vercel.app"
                                        + "/login/callback"
                        )
                        .queryParam(
                                "error",
                                "WITHDRAWN_USER"
                        )
                        .queryParam(
                                "message",
                                "탈퇴한 계정입니다."
                        )
                        .build()
                        .encode(StandardCharsets.UTF_8)
                        .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
