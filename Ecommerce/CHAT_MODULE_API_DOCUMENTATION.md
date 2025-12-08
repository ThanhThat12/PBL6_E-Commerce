# Chat Module API Documentation

## Overview

Complete, production-grade Chat API module for e-commerce marketplace using Spring Boot 3+, Hibernate/JPA, and MySQL.

### Key Features
- Role-agnostic design (no hardcoded roles)
- Permission-based access using user_id, shop_id, and order_id
- Support for ORDER, SHOP, and SUPPORT conversation types
- Ready for WebSocket integration
- Clean architecture with proper separation of concerns

---

## Database Schema

### Tables Created

1. **conversation**
   - id (PK)
   - type (ENUM: ORDER, SHOP, SUPPORT)
   - order_id (nullable, FK to orders)
   - shop_id (nullable, FK to shops)
   - created_by (FK to users)
   - created_at
   - last_activity_at

2. **conversation_member**
   - id (PK)
   - conversation_id (FK)
   - user_id (FK)
   - UNIQUE constraint on (conversation_id, user_id)

3. **message**
   - id (PK)
   - conversation_id (FK)
   - sender_id (FK to users)
   - message_type (ENUM: TEXT, IMAGE)
   - content (TEXT)
   - created_at

---

## API Endpoints

### 1. Create Conversation

**Endpoint:** `POST /api/conversations`

**Description:** Creates a new conversation or returns an existing one.

**Request Body:**
```json
{
  "type": "ORDER",
  "orderId": 123,
  "shopId": 456
}
```

**Conversation Types:**

#### ORDER Conversation
```json
{
  "type": "ORDER",
  "orderId": 123
}
```
- Automatically adds buyer and seller (shop owner)
- Seller determined by order.shop.owner
- Returns existing conversation if one already exists for this order

#### SHOP Conversation
```json
{
  "type": "SHOP",
  "shopId": 456
}
```
- Adds requesting user and shop owner
- Returns existing conversation if one already exists between user and shop

#### SUPPORT Conversation
```json
{
  "type": "SUPPORT",
  "targetUserId": 789  // optional admin ID
}
```
- Adds requesting user
- Optionally adds specific admin
- Returns existing support conversation for this user

**Response:**
```json
{
  "status": 201,
  "error": null,
  "message": "Conversation created successfully",
  "data": {
    "id": 1,
    "type": "ORDER",
    "orderId": 123,
    "shopId": 456,
    "shopName": "My Shop",
    "createdById": 10,
    "createdByName": "John Doe",
    "createdAt": "2025-12-05T10:30:00",
    "lastActivityAt": "2025-12-05T10:30:00",
    "members": [
      {
        "id": 1,
        "userId": 10,
        "userName": "John Doe",
        "userEmail": "john@example.com",
        "userAvatar": "https://example.com/avatar.jpg"
      },
      {
        "id": 2,
        "userId": 20,
        "userName": "Jane Smith",
        "userEmail": "jane@example.com",
        "userAvatar": "https://example.com/avatar2.jpg"
      }
    ],
    "lastMessage": null,
    "messageCount": 0
  }
}
```

---

### 2. Get My Conversations

**Endpoint:** `GET /api/conversations/my`

**Description:** Get all conversations for the current user, sorted by last activity (most recent first).

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Conversations retrieved successfully",
  "data": [
    {
      "id": 1,
      "type": "ORDER",
      "orderId": 123,
      "shopId": 456,
      "shopName": "My Shop",
      "otherParticipantName": "Jane Smith",
      "otherParticipantAvatar": "https://example.com/avatar2.jpg",
      "lastActivityAt": "2025-12-05T14:20:00",
      "lastMessage": {
        "id": 42,
        "conversationId": 1,
        "senderId": 20,
        "senderName": "Jane Smith",
        "senderAvatar": "https://example.com/avatar2.jpg",
        "messageType": "TEXT",
        "content": "Hello, I have a question about my order",
        "createdAt": "2025-12-05T14:20:00"
      },
      "unreadCount": 0
    },
    {
      "id": 2,
      "type": "SHOP",
      "orderId": null,
      "shopId": 789,
      "shopName": "Another Shop",
      "otherParticipantName": "Shop Owner",
      "otherParticipantAvatar": "https://example.com/avatar3.jpg",
      "lastActivityAt": "2025-12-05T12:15:00",
      "lastMessage": {
        "id": 35,
        "conversationId": 2,
        "senderId": 10,
        "senderName": "John Doe",
        "senderAvatar": "https://example.com/avatar.jpg",
        "messageType": "TEXT",
        "content": "Do you have this in stock?",
        "createdAt": "2025-12-05T12:15:00"
      },
      "unreadCount": 0
    }
  ]
}
```

---

### 3. Get Conversation Details

**Endpoint:** `GET /api/conversations/{id}`

**Description:** Get detailed conversation information including all members.

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Conversation details retrieved successfully",
  "data": {
    "id": 1,
    "type": "ORDER",
    "orderId": 123,
    "shopId": 456,
    "shopName": "My Shop",
    "createdById": 10,
    "createdByName": "John Doe",
    "createdAt": "2025-12-05T10:30:00",
    "lastActivityAt": "2025-12-05T14:20:00",
    "members": [
      {
        "id": 1,
        "userId": 10,
        "userName": "John Doe",
        "userEmail": "john@example.com",
        "userAvatar": "https://example.com/avatar.jpg"
      },
      {
        "id": 2,
        "userId": 20,
        "userName": "Jane Smith",
        "userEmail": "jane@example.com",
        "userAvatar": "https://example.com/avatar2.jpg"
      }
    ],
    "lastMessage": {
      "id": 42,
      "conversationId": 1,
      "senderId": 20,
      "senderName": "Jane Smith",
      "senderAvatar": "https://example.com/avatar2.jpg",
      "messageType": "TEXT",
      "content": "Hello, I have a question about my order",
      "createdAt": "2025-12-05T14:20:00"
    },
    "messageCount": 15
  }
}
```

---

### 4. Send Message

**Endpoint:** `POST /api/messages`

**Description:** Send a message in a conversation.

**Validation:**
- Sender must be a conversation member
- Conversation must exist

**Request Body (Text Message):**
```json
{
  "conversationId": 1,
  "messageType": "TEXT",
  "content": "Hello, I have a question about my order"
}
```

**Request Body (Image Message):**
```json
{
  "conversationId": 1,
  "messageType": "IMAGE",
  "content": "https://example.com/image.jpg"
}
```

**Response:**
```json
{
  "status": 201,
  "error": null,
  "message": "Message sent successfully",
  "data": {
    "id": 43,
    "conversationId": 1,
    "senderId": 10,
    "senderName": "John Doe",
    "senderAvatar": "https://example.com/avatar.jpg",
    "messageType": "TEXT",
    "content": "Thank you for your help!",
    "createdAt": "2025-12-05T14:25:00"
  }
}
```

---

### 5. Get Conversation Messages

**Endpoint:** `GET /api/messages/conversation/{conversationId}`

**Description:** Get all messages in a conversation, ordered by creation time (oldest first).

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Messages retrieved successfully",
  "data": [
    {
      "id": 40,
      "conversationId": 1,
      "senderId": 10,
      "senderName": "John Doe",
      "senderAvatar": "https://example.com/avatar.jpg",
      "messageType": "TEXT",
      "content": "Hi, when will my order arrive?",
      "createdAt": "2025-12-05T14:00:00"
    },
    {
      "id": 41,
      "conversationId": 1,
      "senderId": 20,
      "senderName": "Jane Smith",
      "senderAvatar": "https://example.com/avatar2.jpg",
      "messageType": "TEXT",
      "content": "Your order is scheduled for delivery tomorrow.",
      "createdAt": "2025-12-05T14:10:00"
    },
    {
      "id": 42,
      "conversationId": 1,
      "senderId": 10,
      "senderName": "John Doe",
      "senderAvatar": "https://example.com/avatar.jpg",
      "messageType": "IMAGE",
      "content": "https://example.com/product-photo.jpg",
      "createdAt": "2025-12-05T14:15:00"
    }
  ]
}
```

---

### 6. Get Paginated Messages

**Endpoint:** `GET /api/messages/conversation/{conversationId}/paginated`

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Page size
- `sortDirection` (default: asc) - Sort direction (asc or desc)

**Example:** `GET /api/messages/conversation/1/paginated?page=0&size=20&sortDirection=desc`

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Paginated messages retrieved successfully",
  "data": {
    "content": [
      {
        "id": 42,
        "conversationId": 1,
        "senderId": 10,
        "senderName": "John Doe",
        "senderAvatar": "https://example.com/avatar.jpg",
        "messageType": "TEXT",
        "content": "Thank you!",
        "createdAt": "2025-12-05T14:25:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      }
    },
    "totalPages": 1,
    "totalElements": 15,
    "last": true,
    "first": true,
    "size": 20,
    "number": 0,
    "numberOfElements": 15,
    "empty": false
  }
}
```

---

### 7. Get Latest Message

**Endpoint:** `GET /api/messages/conversation/{conversationId}/latest`

**Description:** Get the most recent message in a conversation.

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Latest message retrieved successfully",
  "data": {
    "id": 42,
    "conversationId": 1,
    "senderId": 20,
    "senderName": "Jane Smith",
    "senderAvatar": "https://example.com/avatar2.jpg",
    "messageType": "TEXT",
    "content": "Your order is on the way!",
    "createdAt": "2025-12-05T14:20:00"
  }
}
```

---

### 8. Get Message Count

**Endpoint:** `GET /api/messages/conversation/{conversationId}/count`

**Description:** Get the total number of messages in a conversation.

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Message count retrieved successfully",
  "data": 15
}
```

---

### 9. Delete Message

**Endpoint:** `DELETE /api/messages/{messageId}`

**Description:** Delete a message. User can only delete their own messages.

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Message deleted successfully",
  "data": null
}
```

---

## Error Responses

### Conversation Not Found
```json
{
  "status": 404,
  "error": "CONVERSATION_NOT_FOUND",
  "message": "Conversation not found with ID: 123",
  "data": null
}
```

### Not a Conversation Member
```json
{
  "status": 403,
  "error": "NOT_CONVERSATION_MEMBER",
  "message": "User 10 is not a member of conversation 123",
  "data": null
}
```

### Permission Denied
```json
{
  "status": 403,
  "error": "CONVERSATION_PERMISSION_DENIED",
  "message": "You do not have permission to access this conversation",
  "data": null
}
```

### Invalid Conversation Data
```json
{
  "status": 400,
  "error": "INVALID_CONVERSATION_DATA",
  "message": "Order ID is required for ORDER conversations",
  "data": null
}
```

### Message Not Allowed
```json
{
  "status": 403,
  "error": "MESSAGE_NOT_ALLOWED",
  "message": "You can only delete your own messages",
  "data": null
}
```

---

## Permission Logic

### ORDER Conversations
- **Participants:** Buyer (order.user) + Seller (order.shop.owner)
- **Access:** Only buyer and seller can view/send messages
- **Determination:** Based on order.user_id and order.shop.owner_id

### SHOP Conversations
- **Participants:** Requesting user + Shop owner
- **Access:** Only the two participants can view/send messages
- **Determination:** Based on user_id and shop.owner_id

### SUPPORT Conversations
- **Participants:** User + Admin(s)
- **Access:** User who created it + admins
- **Determination:** Based on conversation.created_by and admin status

**Important:** NO role-based checks. All permissions determined by:
- user_id
- shop.owner_id
- order.user_id
- conversation membership

---

## Implementation Details

### Entities
- `Conversation` - Main conversation entity
- `ConversationMember` - Links users to conversations
- `Message` - Individual messages

### Enums
- `ConversationType` - ORDER, SHOP, SUPPORT
- `MessageType` - TEXT, IMAGE

### Services
- `ConversationService` - Manages conversation creation and retrieval
- `MessageService` - Manages message sending and retrieval

### Utilities
- `ConversationPermissionValidator` - Validates permissions without roles

### Controllers
- `ConversationController` - REST endpoints for conversations
- `MessageController` - REST endpoints for messages

---

## WebSocket Integration (Future)

The current implementation is ready for WebSocket integration:

1. **Existing structure supports real-time messaging:**
   - Conversation tracking with last_activity_at
   - Message ordering by created_at
   - Member validation

2. **To add WebSocket:**
   - Create WebSocket endpoints using Spring WebSocket
   - Subscribe to conversations by ID
   - Broadcast new messages to conversation members
   - Use existing permission validator for WebSocket authentication

3. **Example WebSocket topics:**
   - `/topic/conversations/{conversationId}` - For receiving messages
   - `/app/messages/send` - For sending messages

---

## Testing Examples

### Postman Collection

Create a Postman collection with these requests:

1. **Create ORDER Conversation**
   - POST `/api/conversations`
   - Body: `{"type": "ORDER", "orderId": 1}`

2. **Create SHOP Conversation**
   - POST `/api/conversations`
   - Body: `{"type": "SHOP", "shopId": 1}`

3. **Get My Conversations**
   - GET `/api/conversations/my`

4. **Send Message**
   - POST `/api/messages`
   - Body: `{"conversationId": 1, "messageType": "TEXT", "content": "Hello!"}`

5. **Get Messages**
   - GET `/api/messages/conversation/1`

---

## Database Migrations

### SQL to create tables:

```sql
CREATE TABLE conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    order_id BIGINT,
    shop_id BIGINT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    FOREIGN KEY (shop_id) REFERENCES shop(id),
    FOREIGN KEY (created_by) REFERENCES user(id)
);

CREATE TABLE conversation_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_conversation_member (conversation_id, user_id)
);

CREATE TABLE message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_type VARCHAR(10) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_conversation_order ON conversation(order_id);
CREATE INDEX idx_conversation_shop ON conversation(shop_id);
CREATE INDEX idx_conversation_type ON conversation(type);
CREATE INDEX idx_conversation_last_activity ON conversation(last_activity_at DESC);
CREATE INDEX idx_message_conversation ON message(conversation_id);
CREATE INDEX idx_message_created_at ON message(created_at);
CREATE INDEX idx_conversation_member_user ON conversation_member(user_id);
```

---

## Architecture Benefits

1. **Role-Agnostic:** No hardcoded seller/buyer roles
2. **Flexible:** Supports multiple conversation types
3. **Scalable:** Indexed queries and pagination support
4. **Clean:** Separation of concerns with services, DTOs, validators
5. **Secure:** Permission validation at service layer
6. **Extensible:** Ready for WebSocket, notifications, read receipts
7. **Production-Ready:** Exception handling, logging, transactions

---

## Next Steps

1. **Add WebSocket Support** - Real-time messaging
2. **Implement Read Receipts** - Track message read status
3. **Add Notifications** - Email/push notifications for new messages
4. **Implement Unread Count** - Show unread message count per conversation
5. **Add Message Search** - Search messages by content
6. **Add File Upload** - Support for file attachments
7. **Add Typing Indicators** - Show when user is typing
8. **Add Message Reactions** - Like, emoji reactions to messages

---

## Support

For questions or issues with the Chat Module, please refer to:
- Entity documentation in `domain/` package
- Service documentation in `service/` package
- API documentation (this file)
