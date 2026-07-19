package com.swyp14.phocamatch.auth.token;

import com.swyp14.phocamatch.user.domain.AuthProvider;
import com.swyp14.phocamatch.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.crypto.SecretKey;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final SecretKey secretKey;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-token-expiration-seconds}")
    private long accessTokenExpirationSeconds;

    @Value("${app.jwt.signup-token-expiration-seconds}")
    private long signupTokenExpirationSeconds;

    public String createAccessToken(User user){
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(accessTokenExpirationSeconds);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim(
                        "token_type",
                        TokenType.ACCESS.name()
                )
                .build();

        return encode(claims);
    }

    public String createSignupToken(
            AuthProvider provider,
            String providerUserId,
            String email
    ){
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(signupTokenExpirationSeconds);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(providerUserId)
                .claim(
                        "token_type",
                        TokenType.SIGNUP.name()
                )
                .claim(
                        "provider",
                        provider.name()
                )
                .claim(
                        "email",
                        email
                )
                .build();

        return encode(claims);
    }

    public SignupTokenPayload parsesSignupToken(
            String token
    ){
        NimbusJwtDecoder signupTokenDecoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        OAuth2TokenValidator<Jwt> defaultValidator =
                JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> signupTypeValidator =
                jwt -> {
                    String tokenType =
                            jwt.getClaimAsString("token_type");

                    if("SIGNUP".equals(tokenType)){
                        return OAuth2TokenValidatorResult.success();
                    }

                    OAuth2Error error = new OAuth2Error(
                            "invalid_token",
                            "Signup Token이 아닙니다.",
                            null
                    );

                    return OAuth2TokenValidatorResult.failure(error);
                };

        signupTokenDecoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        defaultValidator,
                        signupTypeValidator
                )
        );

        Jwt jwt = signupTokenDecoder.decode(token);

        String provider = jwt.getClaimAsString("provider");
        String email = jwt.getClaimAsString("email");
        String providerUserId = jwt.getSubject();

        if (provider == null
                || email == null
                || providerUserId == null) {
            throw new JwtValidationException(
                    "Signup Token에 필수 정보가 없습니다.",
                    java.util.List.of(
                            new OAuth2Error("invalid_token")
                    )
            );
        }

        return new SignupTokenPayload(
                AuthProvider.valueOf(provider),
                providerUserId,
                email
        );
    }

    private String encode(JwtClaimsSet claims){
        JwsHeader header =
                JwsHeader
                        .with(MacAlgorithm.HS256)
                        .type("JWT")
                        .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();
    }

    public record SignupTokenPayload(
            AuthProvider provider,
            String providerUserId,
            String email
    ){

    }

}
