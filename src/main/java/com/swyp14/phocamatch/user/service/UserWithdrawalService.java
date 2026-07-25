package com.swyp14.phocamatch.user.service;

import com.swyp14.phocamatch.tradeset.domain.TradeSetStatus;
import com.swyp14.phocamatch.tradeset.repository.TradeSetRepository;
import com.swyp14.phocamatch.user.domain.User;
import com.swyp14.phocamatch.user.exception.AlreadyWithdrawnUserException;
import com.swyp14.phocamatch.user.exception.UserNotFoundException;
import com.swyp14.phocamatch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final UserRepository userRepository;
    private final TradeSetRepository tradeSetRepository;

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.isWithdrawn()) {
            throw new AlreadyWithdrawnUserException();
        }

        tradeSetRepository.deleteActiveTradeSetsByUserId(
                userId,
                TradeSetStatus.ACTIVE,
                TradeSetStatus.DELETED
        );

        user.withdraw();
        userRepository.save(user);
    }
}
