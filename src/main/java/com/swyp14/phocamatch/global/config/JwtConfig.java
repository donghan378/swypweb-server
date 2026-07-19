package com.swyp14.phocamatch.global.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${app.jwt.secret}")
            String encodedSecret
    ) {
        byte[] keyBytes;

        try{
            keyBytes = Base64.getDecoder().decode(encodedSecret);
        }catch (IllegalArgumentException exception){
            throw new IllegalStateException(
                    "JWT_SECRET은 Base64 형식이어야 합니다.",
                    exception
            );
        }

        if(keyBytes.length < 32){
            throw new IllegalStateException(
                    "JWT_SECRET은 256비트 이상이어야 합니다."
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        OctetSequenceKey jwk =
                new OctetSequenceKey.Builder(secretKey)
                        .algorithm(JWSAlgorithm.HS256)
                        .build();

        JWKSource<SecurityContext> jwkSource =
                new ImmutableJWKSet<>(
                        new JWKSet(jwk)
                );

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey secretKey,
            @Value("${app.jwt.issuer}")
            String issuer
    ){
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        OAuth2TokenValidator<Jwt> defaultValidator =
                JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> accessTokenValidator =
                jwt -> {
                    String tokenType = jwt.getClaimAsString("token_type");

                    if("ACCESS".equals(tokenType)){
                        return OAuth2TokenValidatorResult.success();
                    }

                    OAuth2Error error = new OAuth2Error(
                            "invalid_token",
                            "Access Token이 아닙니다.",
                            null
                    );

                    return OAuth2TokenValidatorResult.failure(error);
                };

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        defaultValidator,
                        accessTokenValidator
                )
        );

        return decoder;
    }
}
