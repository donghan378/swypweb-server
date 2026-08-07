package com.swyp14.phocamatch.auth.token;

import com.swyp14.phocamatch.auth.domain.RefreshToken;
import com.swyp14.phocamatch.auth.exception.InvalidRefreshTokenException;
import com.swyp14.phocamatch.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.refresh-token-expiration-seconds}")
    private long refreshTokenExpirationSeconds;

    /**
     * 신규 Refresh Token을 발급하고 DB에는 해시값만 저장한다.
     * 반환값(원문)은 쿠키로만 내려주고 서버에는 절대 원문을 저장하지 않는다.
     */
    @Transactional
    public String issue(Long userId) {
        String rawToken = generateRawToken();
        String tokenHash = hash(rawToken);

        LocalDateTime expiresAt =
                LocalDateTime.now().plusSeconds(refreshTokenExpirationSeconds);

        RefreshToken refreshToken =
                RefreshToken.create(userId, tokenHash, expiresAt);

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    /**
     * 기존 Refresh Token을 검증하고, 유효하면 즉시 폐기한 뒤 새 토큰을 발급한다(회전).
     * 이미 폐기되었거나 만료된 토큰이 재사용되면 탈취 가능성으로 간주하고
     * 해당 사용자의 모든 Refresh Token을 폐기해 강제 재로그인을 유도한다.
     */
    @Transactional
    public RotationResult rotate(String rawToken) {
        String tokenHash = hash(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("유효하지 않은 Refresh Token입니다."));

        LocalDateTime now = LocalDateTime.now();

        if (refreshToken.isRevoked() || refreshToken.isExpired(now)) {
            refreshTokenRepository.deleteAllByUserId(refreshToken.getUserId());
            throw new InvalidRefreshTokenException(
                    "만료되었거나 이미 사용된 Refresh Token입니다. 다시 로그인해 주세요."
            );
        }

        refreshToken.revoke(now);

        String newRawToken = issue(refreshToken.getUserId());

        return new RotationResult(refreshToken.getUserId(), newRawToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        String tokenHash = hash(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(refreshToken -> refreshToken.revoke(LocalDateTime.now()));
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("해시 알고리즘을 사용할 수 없습니다.", exception);
        }
    }

    public record RotationResult(Long userId, String newRawToken) {
    }
}
