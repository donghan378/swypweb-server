package com.swyp14.phocamatch.chat.repository;

import com.swyp14.phocamatch.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("""
            SELECT MAX(message.id)
            FROM ChatMessage message
            WHERE message.chatRoom.id = :chatRoomId
            """)
    Long findLatestMessageId(
            @Param("chatRoomId") Long chatRoomId
    );

    @Query("""
            SELECT COUNT(message.id)
            FROM ChatMessage message
            WHERE message.chatRoom.id = :chatRoomId
              AND message.sender.id <> :userId
              AND message.id > :lastReadMessageId
            """)
    long countUnreadMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId,
            @Param("lastReadMessageId") Long lastReadMessageId
    );

    void deleteAllByChatRoom_ChatRoomId(Long chatRoomId);
}
