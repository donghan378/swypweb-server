package com.swyp14.phocamatch.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_provider_provider_user_id",
                        columnNames = {"provider", "provider_user_id"}
                ),
                @UniqueConstraint(
                        name = "uk_users_nickname",
                        columnNames = "nickname"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_At", nullable = false)
    private LocalDateTime updatedAt;

    public static User createSocialUser(
            AuthProvider provider,
            String providerUserId,
            String email,
            String nickname
    ){
        User user = new User();
        user.provider = provider;
        user.providerUserId = providerUserId;
        user.email = email;
        user.nickname = nickname;

        return user;
    }

    public void updateEmail(String email){
        this.email = email;
    }
    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

    public void updateProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }

    @PrePersist
    private void prePersist(){
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
