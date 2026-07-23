package com.swyp14.phocamatch.chat.repository;

import com.swyp14.phocamatch.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
}
