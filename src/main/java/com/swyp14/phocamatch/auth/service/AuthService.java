package com.swyp14.phocamatch.auth.service;

import com.swyp14.phocamatch.auth.dto.TokenResponse;
import com.swyp14.phocamatch.auth.token.RefreshTokenService;
import com.swyp14.phocamatch.auth.token.TokenService;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.exception.DuplicateNicknameException;
import com.swyp14.phocamatch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public SignupResult signup(
            String signupToken,
            String nickname
    ){
        TokenService.SignupTokenPayload payload =
                tokenService.parsesSignupToken(
                        signupToken
                );

        boolean alreadyJoined =
                userRepository
                        .findByProviderAndProviderUserId(
                                payload.provider(),
                                payload.providerUserId()
                        )
                        .isPresent();

        if(alreadyJoined){
            throw new IllegalStateException(
                    "이미 가입된 사용자입니다."
            );
        }

        User user = User.createSocialUser(
                payload.provider(),
                payload.providerUserId(),
                payload.email(),
                nickname
        );

        try{
            userRepository.saveAndFlush(user);
        }catch (DataIntegrityViolationException exception){
            throw new DuplicateNicknameException();
        }

        String accessToken =
                tokenService.createAccessToken(user);

        String refreshToken =
                refreshTokenService.issue(user.getId());

        return new SignupResult(accessToken, refreshToken);
    }

    public record SignupResult(String accessToken, String refreshToken) {

    }

}
