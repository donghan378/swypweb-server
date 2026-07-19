package com.swyp14.phocamatch.user.controller;

import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.dto.UserResponse;
import com.swyp14.phocamatch.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMyInfo(
            @AuthenticationPrincipal Jwt jwt
    ){
        Long userId;

        try{
            userId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception){
            throw new IllegalArgumentException(
                    "Access Token의 사용자 ID가 올바르지 않습니다."
            );
        }

        User user = userService.getUser(userId);

        return UserResponse.from(user);
    }
}
