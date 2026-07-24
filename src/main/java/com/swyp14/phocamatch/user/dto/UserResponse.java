package com.swyp14.phocamatch.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swyp14.phocamatch.user.domain.User;

import java.time.LocalDateTime;

public record UserResponse(
        @JsonProperty("userId")
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        LocalDateTime createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getCreatedAt()
        );
    }
}
