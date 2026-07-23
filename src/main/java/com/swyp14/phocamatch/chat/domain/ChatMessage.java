package com.swyp14.phocamatch.chat.domain;

import com.swyp14.phocamatch.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_messages",
        indexes = {
                @Index(
                        name = "idx_chat_messages_room_message",
                        columnList = "chat_room_id, message_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "chat_room_id",
            nullable = false
    )
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sender_id",
            nullable = false
    )
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "message_type",
            nullable = false,
            length = 20
    )
    private MessageType messageType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public static ChatMessage createText(
            ChatRoom chatRoom,
            User sender,
            String content
    ) {
        ChatMessage message =
                new ChatMessage();

        message.chatRoom = chatRoom;
        message.sender = sender;
        message.messageType = MessageType.TEXT;
        message.content = content;
        message.imageUrl = null;

        return message;
    }

    public static ChatMessage createImage(
            ChatRoom chatRoom,
            User sender,
            String imageUrl
    ) {
        ChatMessage message =
                new ChatMessage();

        message.chatRoom = chatRoom;
        message.sender = sender;
        message.messageType = MessageType.IMAGE;
        message.content = null;
        message.imageUrl = imageUrl;

        return message;
    }
}
