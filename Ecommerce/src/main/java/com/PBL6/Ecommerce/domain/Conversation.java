package com.PBL6.Ecommerce.domain;

import com.PBL6.Ecommerce.constant.ConversationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a conversation in the chat system.
 * A conversation can be of different types: ORDER, SHOP, or SUPPORT.
 * It may be linked to an order or a shop depending on the type.
 */
@Entity
@Table(name = "conversation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationType type;

    /**
     * Reference to the order if this is an ORDER conversation.
     * Nullable for SHOP and SUPPORT conversations.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * Reference to the shop if this is a SHOP or ORDER conversation.
     * Nullable for SUPPORT conversations.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    /**
     * The user who created this conversation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last activity (message sent) in this conversation.
     * Used for sorting conversations by recent activity.
     */
    @Column(nullable = false)
    private LocalDateTime lastActivityAt;

    /**
     * List of members participating in this conversation.
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ConversationMember> members = new ArrayList<>();

    /**
     * List of messages in this conversation.
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (lastActivityAt == null) {
            lastActivityAt = LocalDateTime.now();
        }
    }

    /**
     * Update the last activity timestamp when a new message is sent.
     */
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
}
