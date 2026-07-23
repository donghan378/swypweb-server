package com.swyp14.phocamatch.chat.domain;

import com.swyp14.phocamatch.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "chat_room_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_room_members_room_user",
                        columnNames = {
                                "chat_room_id",
                                "user_id"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "chat_room_id",
            nullable = false
    )
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    public void updateLastReadMessageId(
            Long messageId
    ) {
        if (messageId == null) {
            return;
        }

        if (
                this.lastReadMessageId == null
                        || messageId > this.lastReadMessageId
        ) {
            this.lastReadMessageId = messageId;
        }
    }

    public static ChatRoomMember create(
            ChatRoom chatRoom,
            User user
    ) {
        ChatRoomMember member =
                new ChatRoomMember();

        member.chatRoom = chatRoom;
        member.user = user;

        return member;
    }
}
