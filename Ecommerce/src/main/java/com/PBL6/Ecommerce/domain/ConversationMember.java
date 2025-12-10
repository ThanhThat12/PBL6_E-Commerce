package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a member of a conversation.
 * Links users to conversations they are participating in.
 */
@Entity
@Table(
    name = "conversation_member",
    uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Helper method to check if this member is the specified user.
     */
    public boolean isUser(Long userId) {
        return this.user.getId().equals(userId);
    }
}
