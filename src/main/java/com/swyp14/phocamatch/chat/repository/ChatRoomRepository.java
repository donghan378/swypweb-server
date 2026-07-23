package com.swyp14.phocamatch.chat.repository;

import com.swyp14.phocamatch.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
            SELECT room
            FROM ChatRoom room
            JOIN FETCH room.tradeProposal proposal
            JOIN FETCH proposal.proposer
            JOIN FETCH proposal.receiver
            WHERE room.id = :chatRoomId
            """)
    Optional<ChatRoom> findHeaderById(
            @Param("chatRoomId") Long chatRoomId
    );
}
