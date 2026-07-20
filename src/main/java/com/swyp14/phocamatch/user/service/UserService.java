package com.swyp14.phocamatch.user.service;

import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.dto.NicknameUpdateResponse;
import com.swyp14.phocamatch.user.exception.DuplicateNicknameException;
import com.swyp14.phocamatch.user.exception.UserNotFoundException;
import com.swyp14.phocamatch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser(Long userId){
        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );
    }

    @Transactional
    public NicknameUpdateResponse updateMyNickname(
            Long userId,
            String nickname
    ){
        User user = userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);

        boolean nicknameDuplicated =
                userRepository.existsByNicknameAndIdNot(nickname, userId);

        if(nicknameDuplicated){
            throw new DuplicateNicknameException();
        }

        user.updateNickname(nickname);

        return new NicknameUpdateResponse(
                user.getNickname()
        );
    }
}
