package com.swyp14.phocamatch.user.repository;

import com.swyp14.phocamatch.user.domain.AuthProvider;
import com.swyp14.phocamatch.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );

    boolean existsByNickname(String nickname);
}
