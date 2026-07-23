package com.swyp14.phocamatch.chat.repository;

import com.swyp14.phocamatch.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
