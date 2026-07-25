package com.swyp14.phocamatch.user.controller;

import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.dto.NicknameUpdateRequest;
import com.swyp14.phocamatch.user.dto.NicknameUpdateResponse;
import com.swyp14.phocamatch.user.dto.UserResponse;
import com.swyp14.phocamatch.user.exception.AlreadyWithdrawnUserException;
import com.swyp14.phocamatch.user.exception.DuplicateNicknameException;
import com.swyp14.phocamatch.user.exception.UserNotFoundException;
import com.swyp14.phocamatch.user.service.UserService;
import com.swyp14.phocamatch.user.service.UserWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserWithdrawalService userWithdrawalService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(
            @AuthenticationPrincipal Jwt jwt
    ){
        Long userId;

        try{
            userId = Long.valueOf(jwt.getSubject());

            User user = userService.getUser(userId);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "프로필 조회 성공",
                                    UserResponse.from(user)
                            )
                    );
        } catch (UserNotFoundException exception){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("404", exception.getMessage()));
        }


    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<?> updateMyNickname(
            @AuthenticationPrincipal Jwt jwt,
            @Valid
            @RequestBody NicknameUpdateRequest request
    ){
        Long userId;
        try{
            userId = Long.valueOf(jwt.getSubject());

            NicknameUpdateResponse response =
                    userService.updateMyNickname(
                            userId,
                            request.nickname()
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "닉네임 수정 완료",
                                    response
                            )
                    );
        }catch (DuplicateNicknameException exception){

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of("RESOURCE_002", exception.getMessage()));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> withdraw(
            @AuthenticationPrincipal Jwt jwt
    ) {
        try{
            Long userId = Long.valueOf(jwt.getSubject());

            userWithdrawalService.withdraw(userId);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "회원 탈퇴가 완료되었습니다.",
                                    null
                            )
                    );
        }catch(AlreadyWithdrawnUserException e){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ErrorResponse.of("403", e.getMessage())
                    );
        }
    }

}
