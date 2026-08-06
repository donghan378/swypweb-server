package com.swyp14.phocamatch.chat.repository;

import com.swyp14.phocamatch.chat.domain.ChatRoomMember;
import com.swyp14.phocamatch.chat.dto.ChatRoomListProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    @Query(
            value = """
                    WITH room_summary AS (
                        SELECT
                            my_member.chat_room_id AS chatId,
                            partner.user_id AS partnerUserId,
                            partner.nickname AS partnerNickname,
                            partner.profile_image_url AS partnerProfileImageUrl,

                            last_message.message_type AS lastMessageType,
                            last_message.content AS lastMessageContent,

                            COALESCE(
                                last_message.created_at,
                                room.created_at
                            ) AS lastMessageAt,

                            (
                                SELECT COUNT(*)
                                FROM chat_messages unread_message
                                WHERE unread_message.chat_room_id
                                      = room.chat_room_id
                                  AND unread_message.sender_id <> :userId
                                  AND unread_message.message_id
                                      > COALESCE(
                                            my_member.last_read_message_id,
                                            0
                                        )
                            ) AS unreadCount,

                            proposal.status AS proposalStatus

                        FROM chat_room_members my_member

                        JOIN chat_rooms room
                          ON room.chat_room_id
                           = my_member.chat_room_id

                        JOIN trade_proposals proposal
                          ON proposal.trade_proposal_id
                           = room.trade_proposal_id

                        JOIN chat_room_members partner_member
                          ON partner_member.chat_room_id
                           = room.chat_room_id
                         AND partner_member.user_id <> :userId

                        JOIN users partner
                          ON partner.user_id
                           = partner_member.user_id

                        LEFT JOIN chat_messages last_message
                          ON last_message.message_id = (
                                SELECT message.message_id
                                FROM chat_messages message
                                WHERE message.chat_room_id
                                      = room.chat_room_id
                                ORDER BY message.message_id DESC
                                LIMIT 1
                          )

                        WHERE my_member.user_id = :userId
                    )

                    SELECT
                        chatId,
                        partnerUserId,
                        partnerNickname,
                        partnerProfileImageUrl,
                        lastMessageType,
                        lastMessageContent,
                        lastMessageAt,
                        unreadCount,
                        proposalStatus

                    FROM room_summary

                    WHERE
                        :cursorAt IS NULL
                        OR lastMessageAt < :cursorAt
                        OR (
                            lastMessageAt = :cursorAt
                            AND chatId < :cursorChatId
                        )

                    ORDER BY
                        lastMessageAt DESC,
                        chatId DESC

                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<ChatRoomListProjection> findChatRooms(
            @Param("userId") Long userId,
            @Param("cursorAt") LocalDateTime cursorAt,
            @Param("cursorChatId") Long cursorChatId,
            @Param("limit") int limit
    );

    boolean existsByChatRoom_IdAndUser_Id(
            Long chatRoomId,
            Long userId
    );

    Optional<ChatRoomMember>
    findByChatRoom_IdAndUser_Id(
            Long chatRoomId,
            Long userId
    );

    Optional<ChatRoomMember> findByChatRoom_ChatRoomIdAndUserId(
            Long chatRoomId,
            Long userId
    );

    List<ChatRoomMember> findAllByChatRoom_ChatRoomId(
            Long chatRoomId
    );

    void deleteAllByChatRoom_ChatRoomId(
            Long chatRoomId
    );

}
