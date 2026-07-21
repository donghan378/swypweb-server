package com.swyp14.phocamatch.album.controller;

import com.swyp14.phocamatch.album.dto.AlbumVersionListResponse;
import com.swyp14.phocamatch.album.dto.GroupAlbumListResponse;
import com.swyp14.phocamatch.album.exception.AlbumNotFoundException;
import com.swyp14.phocamatch.album.service.AlbumService;
import com.swyp14.phocamatch.global.error.ErrorResponse;
import com.swyp14.phocamatch.global.response.ApiResponse;
import com.swyp14.phocamatch.idolgroup.exception.IdolGroupNotFoundException;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/collections")
@RequiredArgsConstructor
@Validated
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping("/groups/{groupId}/albums")
    public ResponseEntity<?>
    getGroupAlbums(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "groupId는 양수여야 합니다.")
            Long groupId
    ) {
        try{
            Long userId = parseUserId(jwt);

            GroupAlbumListResponse response =
                    albumService.getGroupAlbums(
                            userId,
                            groupId
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "그룹 앨범 목록 조회 성공",
                                    response
                            )
                    );
        }catch(IdolGroupNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ErrorResponse.of("RESOURCE_001",e.getMessage())
                    );
        }
    }

    @GetMapping("/albums/{albumId}/versions")
    public ResponseEntity<?>
    getAlbumVersions(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            @Positive(message = "albumId는 양수여야 합니다.")
            Long albumId
    ) {
        try{
            Long userId = parseUserId(jwt);

            AlbumVersionListResponse response =
                    albumService.getAlbumVersions(
                            userId,
                            albumId
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            ApiResponse.success(
                                    200,
                                    "앨범 버전 목록 조회 성공",
                                    response
                            )
                    );
        }catch(AlbumNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ErrorResponse.of("RESOURCE_001",e.getMessage())
                    );
        }
    }

    private Long parseUserId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (
                NullPointerException
                | NumberFormatException exception
        ) {
            throw new IllegalArgumentException(
                    "Access Token의 사용자 ID가 올바르지 않습니다."
            );
        }
    }
}
