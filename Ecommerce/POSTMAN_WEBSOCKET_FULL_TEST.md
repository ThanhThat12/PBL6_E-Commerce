# Hướng Dẫn Test WebSocket Bằng Postman - Từ Đầu Đến Cuối 🚀

## 📋 Mục Lục
1. [Tạo Conversation qua REST API](#bước-1-tạo-conversation-qua-rest-api)
2. [Lấy JWT Token](#bước-2-lấy-jwt-token)
3. [Kết nối WebSocket](#bước-3-kết-nối-websocket)
4. [Subscribe nhận tin nhắn](#bước-4-subscribe-nhận-tin-nhắn)
5. [Gửi tin nhắn](#bước-5-gửi-tin-nhắn)
6. [Test typing indicator](#bước-6-test-typing-indicator)
7. [Test với nhiều clients](#bước-7-test-với-nhiều-clients)

---

## 🎯 BƯỚC 1: Tạo Conversation Qua REST API

### 1.1. Mở Postman

1. Mở ứng dụng Postman
2. Tạo **New Request**
3. Chọn method: **POST**

### 1.2. Tạo ORDER Conversation (Buyer - Seller)

**URL:**
```
POST http://localhost:8080/api/conversations
```

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

**Body (raw - JSON):**
```json
{
  "type": "ORDER",
  "orderId": 1,
  "shopId": 5
}
```

**Giải thích:**
- `type`: "ORDER" - Cuộc trò chuyện về đơn hàng
- `orderId`: ID đơn hàng (phải tồn tại trong database)
- `shopId`: ID shop bán hàng (phải tồn tại)

**Response thành công (201 Created):**
```json
{
  "id": 1,
  "type": "ORDER",
  "orderId": 1,
  "shopId": 5,
  "userId": null,
  "createdAt": "2025-12-05T14:00:00",
  "lastActivityAt": "2025-12-05T14:00:00",
  "members": [
    {
      "userId": 10,
      "userName": "Nguyễn Văn A",
      "userAvatar": "https://example.com/avatar1.jpg",
      "joinedAt": "2025-12-05T14:00:00"
    },
    {
      "userId": 5,
      "userName": "Shop ABC",
      "userAvatar": "https://example.com/shop-avatar.jpg",
      "joinedAt": "2025-12-05T14:00:00"
    }
  ]
}
```

**⚠️ Lưu lại `id` của conversation này (ví dụ: `1`)**

### 1.3. Tạo SHOP Conversation (Customer - Shop)

**Body (raw - JSON):**
```json
{
  "type": "SHOP",
  "shopId": 5
}
```

**Giải thích:**
- Khách hàng muốn hỏi shop về sản phẩm
- Chỉ cần `shopId`, không cần `orderId`

### 1.4. Tạo SUPPORT Conversation (User - Admin)

**Body (raw - JSON):**
```json
{
  "type": "SUPPORT"
}
```

**Giải thích:**
- User liên hệ admin để được hỗ trợ
- Không cần `orderId` hay `shopId`

---

## 🔐 BƯỚC 2: Lấy JWT Token

### 2.1. Đăng Nhập Để Lấy Token

**Nếu bạn đã có login API:**

**URL:**
```
POST http://localhost:8080/api/auth/login
```

**Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMCIsIm5hbWUiOiJOZ3V54buFbiBWxINuIEEiLCJpYXQiOjE3MzM0MDAwMDB9.xxx",
  "userId": 10,
  "fullName": "Nguyễn Văn A"
}
```

**⚠️ Copy token này để dùng cho WebSocket**

### 2.2. Kiểm Tra Token

Để biết user ID trong token:

1. Truy cập: https://jwt.io/
2. Paste token vào
3. Xem phần `Payload`:
```json
{
  "sub": "10",    // ← Đây là user ID
  "name": "Nguyễn Văn A",
  "iat": 1733400000
}
```

---

## 🔌 BƯỚC 3: Kết Nối WebSocket

### 3.1. Tạo WebSocket Request

1. Trong Postman, click **New** → **WebSocket Request**
2. Hoặc click vào tab **WebSocket** ở sidebar

### 3.2. Cấu Hình Connection

**URL:**
```
ws://localhost:8080/ws
```

### 3.3. Thêm Headers (QUAN TRỌNG!)

1. Click vào tab **Headers**
2. Thêm header mới:

| Key | Value |
|-----|-------|
| `Authorization` | `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` |

**⚠️ Lưu ý:**
- Phải có chữ `Bearer` + dấu cách + token
- Token phải chưa hết hạn
- Token phải hợp lệ

### 3.4. Connect

1. Click nút **Connect**
2. Chờ vài giây
3. Nếu thành công, sẽ thấy:

```
Connected to ws://localhost:8080/ws
```

**Status:** 🟢 Connected

### 3.5. Xử Lý Lỗi Kết Nối

**Nếu thấy lỗi "Connection failed":**
- ✅ Kiểm tra server đang chạy: http://localhost:8080
- ✅ Kiểm tra JWT token đúng format
- ✅ Kiểm tra token chưa hết hạn
- ✅ Xem console log server có lỗi gì

---

## 📩 BƯỚC 4: Subscribe Nhận Tin Nhắn

### 4.1. Subscribe Conversation Messages

Trong tab **Messages** của Postman WebSocket:

1. Click **New Message**
2. Chọn **Text**
3. Nhập nội dung:

```json
SUBSCRIBE
id:sub-messages
destination:/topic/conversations/1

```

**⚠️ Lưu ý định dạng:**
- Dòng 1: `SUBSCRIBE`
- Dòng 2: `id:sub-messages` (tùy chọn, đặt tên gì cũng được)
- Dòng 3: `destination:/topic/conversations/1` (thay `1` bằng conversation ID)
- Dòng 4: **PHẢI CÓ 1 DÒNG TRỐNG**

### 4.2. Subscribe Typing Indicators

Tạo message mới:

```json
SUBSCRIBE
id:sub-typing
destination:/topic/conversations/1/typing

```

### 4.3. Subscribe Delivery Confirmations

Tạo message mới:

```json
SUBSCRIBE
id:sub-confirm
destination:/user/queue/confirmations

```

### 4.4. Subscribe Private Notifications

Tạo message mới:

```json
SUBSCRIBE
id:sub-notifications
destination:/user/queue/notifications

```

### 4.5. Kiểm Tra Subscribe Thành Công

Sau khi gửi các lệnh SUBSCRIBE, bạn sẽ thấy trong **Messages** list:

```
➤ Sent: SUBSCRIBE...
← Received: (subscribe confirmation frame)
```

---

## 💬 BƯỚC 5: Gửi Tin Nhắn

### 5.1. Gửi Text Message

**Format STOMP:**

```
SEND
destination:/app/chat/send
content-type:application/json

{"conversationId":1,"senderId":10,"messageType":"TEXT","content":"Xin chào! Tôi muốn hỏi về đơn hàng"}
```

**⚠️ Lưu ý:**
- Dòng 1: `SEND`
- Dòng 2: `destination:/app/chat/send`
- Dòng 3: `content-type:application/json`
- Dòng 4: **PHẢI CÓ 1 DÒNG TRỐNG**
- Dòng 5: JSON payload (viết 1 dòng, không xuống dòng)

**Giải thích các trường:**
- `conversationId`: ID conversation vừa tạo (ví dụ: 1)
- `senderId`: User ID trong JWT token (ví dụ: 10)
- `messageType`: "TEXT" hoặc "IMAGE"
- `content`: Nội dung tin nhắn

### 5.2. Kết Quả Mong Đợi

Sau khi gửi, bạn sẽ nhận được **2 messages**:

**1. Broadcast Message (tất cả members nhận):**

```json
{
  "id": 1,
  "conversationId": 1,
  "senderId": 10,
  "senderName": "Nguyễn Văn A",
  "senderAvatar": "https://example.com/avatar1.jpg",
  "messageType": "TEXT",
  "content": "Xin chào! Tôi muốn hỏi về đơn hàng",
  "createdAt": "2025-12-05T14:30:00",
  "status": "SENT"
}
```

Destination: `/topic/conversations/1`

**2. Delivery Confirmation (chỉ người gửi nhận):**

```json
{
  "messageId": 1,
  "status": "DELIVERED"
}
```

Destination: `/user/queue/confirmations`

### 5.3. Gửi Image Message

```
SEND
destination:/app/chat/send
content-type:application/json

{"conversationId":1,"senderId":10,"messageType":"IMAGE","content":"https://example.com/product-image.jpg"}
```

**Kết quả tương tự, nhưng `messageType` = "IMAGE"**

### 5.4. Gửi Nhiều Tin Nhắn

Cứ lặp lại format trên, thay đổi `content`:

```
{"conversationId":1,"senderId":10,"messageType":"TEXT","content":"Đơn hàng của tôi đến khi nào?"}
```

```
{"conversationId":1,"senderId":10,"messageType":"TEXT","content":"Tôi muốn đổi địa chỉ giao hàng"}
```

---

## ⌨️ BƯỚC 6: Test Typing Indicator

### 6.1. Gửi "Đang Gõ"

```
SEND
destination:/app/chat/typing
content-type:application/json

{"conversationId":1,"userId":10,"typing":true}
```

### 6.2. Kết Quả

Tất cả members sẽ nhận được:

```json
{
  "conversationId": 1,
  "userId": 10,
  "userName": "Nguyễn Văn A",
  "typing": true
}
```

Destination: `/topic/conversations/1/typing`

### 6.3. Gửi "Dừng Gõ"

```
SEND
destination:/app/chat/typing
content-type:application/json

{"conversationId":1,"userId":10,"typing":false}
```

**Kết quả:** `typing: false`

---

## 👥 BƯỚC 7: Test Với Nhiều Clients

### 7.1. Mở 2 Cửa Sổ Postman

**Cửa sổ 1:** User A (ID = 10) - Buyer
**Cửa sổ 2:** User B (ID = 5) - Seller

### 7.2. Lấy Token Cho Cả 2 Users

**User A:**
- Login với tài khoản buyer
- Lấy token A

**User B:**
- Login với tài khoản seller
- Lấy token B

### 7.3. Kết Nối Cả 2 WebSocket

**Cửa sổ 1:**
```
URL: ws://localhost:8080/ws
Header: Authorization: Bearer TOKEN_A
```

**Cửa sổ 2:**
```
URL: ws://localhost:8080/ws
Header: Authorization: Bearer TOKEN_B
```

### 7.4. Subscribe Cả 2

**Cả 2 cửa sổ đều gửi:**
```
SUBSCRIBE
id:sub-1
destination:/topic/conversations/1

```

### 7.5. Test Real-time Chat

**Cửa sổ 1 (User A) gửi:**
```
SEND
destination:/app/chat/send
content-type:application/json

{"conversationId":1,"senderId":10,"messageType":"TEXT","content":"Xin chào shop!"}
```

**Kết quả:**
- ✅ Cửa sổ 1 nhận broadcast message
- ✅ Cửa sổ 1 nhận delivery confirmation
- ✅ **Cửa sổ 2 cũng nhận broadcast message** ← Real-time!

**Cửa sổ 2 (User B) reply:**
```
SEND
destination:/app/chat/send
content-type:application/json

{"conversationId":1,"senderId":5,"messageType":"TEXT","content":"Xin chào! Tôi có thể giúp gì cho bạn?"}
```

**Kết quả:**
- ✅ Cửa sổ 2 nhận broadcast + confirmation
- ✅ **Cửa sổ 1 nhận broadcast message ngay lập tức**

### 7.6. Test Typing Between 2 Users

**Cửa sổ 1 gửi typing:**
```
{"conversationId":1,"userId":10,"typing":true}
```

**→ Cửa sổ 2 thấy "User A đang gõ..."**

**Cửa sổ 2 gửi typing:**
```
{"conversationId":1,"userId":5,"typing":true}
```

**→ Cửa sổ 1 thấy "Shop B đang gõ..."**

---

## 🧪 BƯỚC 8: Test Cases Đầy Đủ

### Test Case 1: Order Conversation Chat

**Scenario:** Buyer hỏi seller về đơn hàng

1. **Tạo conversation:**
   ```json
   POST /api/conversations
   {
     "type": "ORDER",
     "orderId": 1,
     "shopId": 5
   }
   ```

2. **Buyer connect:** ws://localhost:8080/ws (token của buyer)

3. **Seller connect:** ws://localhost:8080/ws (token của seller)

4. **Cả 2 subscribe:** `/topic/conversations/1`

5. **Buyer gửi:**
   ```json
   {
     "conversationId": 1,
     "senderId": 10,
     "messageType": "TEXT",
     "content": "Đơn hàng #1 của tôi đến khi nào?"
   }
   ```

6. **Seller nhận được tin nhắn** ✅

7. **Seller reply:**
   ```json
   {
     "conversationId": 1,
     "senderId": 5,
     "messageType": "TEXT",
     "content": "Đơn hàng sẽ đến trong 2-3 ngày"
   }
   ```

8. **Buyer nhận được reply** ✅

**✅ Pass:** Real-time chat hoạt động

---

### Test Case 2: Shop Inquiry Conversation

**Scenario:** Customer hỏi shop về sản phẩm

1. **Tạo conversation:**
   ```json
   POST /api/conversations
   {
     "type": "SHOP",
     "shopId": 5
   }
   ```

2. **Customer gửi:**
   ```json
   {
     "conversationId": 2,
     "senderId": 15,
     "messageType": "TEXT",
     "content": "Sản phẩm này còn hàng không?"
   }
   ```

3. **Shop owner reply:**
   ```json
   {
     "conversationId": 2,
     "senderId": 5,
     "messageType": "TEXT",
     "content": "Còn hàng ạ! Bạn muốn mua size nào?"
   }
   ```

**✅ Pass:** Shop chat hoạt động

---

### Test Case 3: Support Conversation

**Scenario:** User liên hệ admin

1. **Tạo conversation:**
   ```json
   POST /api/conversations
   {
     "type": "SUPPORT"
   }
   ```

2. **User gửi:**
   ```json
   {
     "conversationId": 3,
     "senderId": 10,
     "messageType": "TEXT",
     "content": "Tài khoản của tôi bị khóa"
   }
   ```

3. **Admin reply:**
   ```json
   {
     "conversationId": 3,
     "senderId": 1,
     "messageType": "TEXT",
     "content": "Tôi sẽ kiểm tra ngay"
   }
   ```

**✅ Pass:** Support chat hoạt động

---

### Test Case 4: Typing Indicator

1. **User A gửi typing:**
   ```json
   {
     "conversationId": 1,
     "userId": 10,
     "typing": true
   }
   ```

2. **User B nhận typing notification** ✅

3. **Sau 2 giây, User A gửi:**
   ```json
   {
     "conversationId": 1,
     "userId": 10,
     "typing": false
   }
   ```

4. **User B không thấy typing nữa** ✅

**✅ Pass:** Typing indicator hoạt động

---

### Test Case 5: Image Message

1. **Upload ảnh lên Cloudinary/server (qua REST API)**

2. **Lấy URL ảnh:** `https://example.com/chat-image.jpg`

3. **Gửi qua WebSocket:**
   ```json
   {
     "conversationId": 1,
     "senderId": 10,
     "messageType": "IMAGE",
     "content": "https://example.com/chat-image.jpg"
   }
   ```

4. **User B nhận message với `messageType: "IMAGE"`** ✅

**✅ Pass:** Image message hoạt động

---

### Test Case 6: Permission Validation

**Scenario:** User C (không phải member) cố gửi tin nhắn

1. **User C connect với token của mình**

2. **User C gửi:**
   ```json
   {
     "conversationId": 1,
     "senderId": 99,
     "messageType": "TEXT",
     "content": "Hack message!"
   }
   ```

3. **Kết quả:** Server reject, gửi error message

**✅ Pass:** Permission check hoạt động

---

### Test Case 7: Sender ID Mismatch

**Scenario:** User giả mạo senderId

1. **User A (ID = 10) gửi với senderId khác:**
   ```json
   {
     "conversationId": 1,
     "senderId": 999,
     "messageType": "TEXT",
     "content": "Fake message"
   }
   ```

2. **Kết quả:** Server reject vì senderId không khớp với JWT

**✅ Pass:** Security validation hoạt động

---

## 📊 Checklist Hoàn Chỉnh

### Chuẩn Bị

- [ ] Server Spring Boot đang chạy (port 8080)
- [ ] Database có bảng: `conversations`, `conversation_members`, `messages`
- [ ] Có ít nhất 2 user accounts (buyer + seller)
- [ ] Có ít nhất 1 order trong database
- [ ] Có ít nhất 1 shop trong database
- [ ] Postman đã cài đặt

### Tạo Conversation

- [ ] Tạo được ORDER conversation
- [ ] Tạo được SHOP conversation
- [ ] Tạo được SUPPORT conversation
- [ ] Response trả về đúng format
- [ ] Members được tự động thêm vào

### Kết Nối WebSocket

- [ ] Connect thành công với JWT token
- [ ] Headers có Authorization
- [ ] Status hiển thị "Connected"
- [ ] Không có error trong console

### Subscribe

- [ ] Subscribe `/topic/conversations/{id}` thành công
- [ ] Subscribe `/topic/conversations/{id}/typing` thành công
- [ ] Subscribe `/user/queue/confirmations` thành công
- [ ] Không có error message

### Gửi Tin Nhắn

- [ ] Gửi text message thành công
- [ ] Nhận broadcast message
- [ ] Nhận delivery confirmation
- [ ] Message được lưu vào database
- [ ] `last_activity_at` được update

### Typing Indicator

- [ ] Gửi typing = true
- [ ] Nhận typing notification
- [ ] Gửi typing = false
- [ ] Typing indicator biến mất

### Multi-Client

- [ ] 2 clients kết nối cùng lúc
- [ ] Client A gửi → Client B nhận
- [ ] Client B gửi → Client A nhận
- [ ] Real-time không delay

### Security

- [ ] Token sai → connection failed
- [ ] SenderId sai → rejected
- [ ] Không phải member → rejected
- [ ] Token hết hạn → connection failed

---

## 🐛 Troubleshooting

### Lỗi: "Connection refused"

**Nguyên nhân:** Server không chạy

**Giải pháp:**
```bash
# Kiểm tra server
curl http://localhost:8080/actuator/health

# Hoặc
Test-NetConnection localhost -Port 8080
```

---

### Lỗi: "Missing or invalid Authorization header"

**Nguyên nhân:** JWT token không đúng

**Giải pháp:**
1. Kiểm tra format: `Bearer <token>`
2. Không có dấu cách thừa
3. Token chưa hết hạn
4. Test token bằng REST API trước

---

### Lỗi: "senderId mismatch"

**Nguyên nhân:** senderId khác user ID trong token

**Giải pháp:**
1. Parse JWT token: https://jwt.io/
2. Lấy `sub` (subject) = user ID
3. Dùng đúng user ID khi gửi

---

### Lỗi: "Access Denied"

**Nguyên nhân:** User không phải member

**Giải pháp:**
```sql
-- Check database
SELECT * FROM conversation_members 
WHERE conversation_id = 1 AND user_id = 10;

-- Nếu không có → add member
INSERT INTO conversation_members (conversation_id, user_id, joined_at)
VALUES (1, 10, NOW());
```

---

### Lỗi: "Conversation not found"

**Nguyên nhân:** Conversation ID không tồn tại

**Giải pháp:**
```sql
-- Check database
SELECT * FROM conversations WHERE id = 1;

-- Hoặc dùng REST API
GET http://localhost:8080/api/conversations/my
```

---

### Messages không nhận được

**Nguyên nhân:** Chưa subscribe đúng topic

**Giải pháp:**
1. Subscribe lại: `/topic/conversations/{id}`
2. Đảm bảo conversation ID đúng
3. Check Postman Messages tab
4. Xem có error frame không

---

## 📸 Screenshots Guide

### 1. Postman WebSocket - Connect Tab

```
┌─────────────────────────────────────────┐
│ WebSocket Request                       │
├─────────────────────────────────────────┤
│ ws://localhost:8080/ws         [Connect]│
│                                         │
│ Headers:                                │
│ Authorization | Bearer eyJhbGci...      │
│                                         │
│ Status: 🟢 Connected                    │
└─────────────────────────────────────────┘
```

### 2. Postman WebSocket - Messages Tab

```
┌─────────────────────────────────────────┐
│ Messages                      [New Message]
├─────────────────────────────────────────┤
│ ➤ Sent: SUBSCRIBE...                   │
│ ← Received: (frame)                     │
│                                         │
│ ➤ Sent: SEND /app/chat/send            │
│ ← Received: {                           │
│     "id": 1,                            │
│     "content": "Hello"                  │
│   }                                     │
└─────────────────────────────────────────┘
```

---

## 🎯 Kết Luận

### Bạn Đã Test Thành Công Khi:

✅ Tạo được conversation qua REST API  
✅ Kết nối WebSocket với JWT token  
✅ Subscribe được các topics  
✅ Gửi tin nhắn text + image  
✅ Nhận được broadcast messages  
✅ Nhận được delivery confirmations  
✅ Typing indicator hoạt động  
✅ Multi-client chat real-time  
✅ Permission validation chặn được unauthorized access  

### Thời Gian Test:

- **Setup:** 5 phút
- **Test cơ bản:** 10 phút
- **Test đầy đủ:** 30 phút

### Next Steps:

1. ✅ Integrate vào React/React Native frontend
2. ✅ Thêm read receipts
3. ✅ Thêm message reactions
4. ✅ Thêm file upload
5. ✅ Deploy lên production

---

**Chúc bạn test thành công! 🚀**

Nếu gặp lỗi, xem lại phần **Troubleshooting** hoặc check server logs!
