package com.swyp14.phocamatch.chat.repository;

import com.swyp14.phocamatch.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage>
    findByChatRoom_IdOrderByIdDesc(
            Long chatRoomId,
            Pageable pageable
    );

    List<ChatMessage>
    findByChatRoom_IdAndIdLessThanOrderByIdDesc(
            Long chatRoomId,
            Long cursor,
            Pageable pageable
    );
}
