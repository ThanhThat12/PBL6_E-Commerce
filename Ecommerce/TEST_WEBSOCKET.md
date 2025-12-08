# Hướng Dẫn Test WebSocket Chat - Tiếng Việt 🇻🇳

## 🎯 3 Cách Test WebSocket

1. **Postman** - Dễ nhất, có giao diện đồ họa
2. **Browser Console** - Nhanh, không cần cài gì
3. **Test Code Frontend** - Tích hợp thực tế

---

## 1️⃣ Test Bằng POSTMAN (Khuyến Nghị) ⭐

### Bước 1: Mở Postman WebSocket Request

1. Mở Postman
2. Click **New** → **WebSocket Request**
3. Nhập URL: `ws://localhost:8080/ws`

### Bước 2: Kết Nối Với JWT Token

1. Trước khi click **Connect**, thêm **Headers**:
   - Click tab **Headers**
   - Thêm header:
     ```
     Key: Authorization
     Value: Bearer YOUR_JWT_TOKEN
     ```

2. Click **Connect**

3. Nếu thành công, sẽ thấy:
   ```
   Connected to ws://localhost:8080/ws
   ```

### Bước 3: Subscribe Nhận Tin Nhắn

Trong tab **Messages**, gửi lệnh SUBSCRIBE:

```json
{
  "command": "SUBSCRIBE",
  "headers": {
    "id": "sub-1",
    "destination": "/topic/conversations/1"
  }
}
```

**Giải thích:**
- `destination: /topic/conversations/1` - Nhận tin nhắn từ conversation ID = 1
- Thay số `1` bằng ID conversation bạn muốn test

### Bước 4: Subscribe Typing Indicator

```json
{
  "command": "SUBSCRIBE",
  "headers": {
    "id": "sub-2",
    "destination": "/topic/conversations/1/typing"
  }
}
```

### Bước 5: Gửi Tin Nhắn

```json
{
  "command": "SEND",
  "headers": {
    "destination": "/app/chat/send"
  },
  "body": "{\"conversationId\":1,\"senderId\":10,\"messageType\":\"TEXT\",\"content\":\"Xin chào! Đây là tin nhắn test\"}"
}
```

**Lưu ý:**
- `senderId` phải trùng với user ID trong JWT token
- `conversationId` phải tồn tại trong database
- User phải là thành viên của conversation

### Bước 6: Gửi Typing Indicator

```json
{
  "command": "SEND",
  "headers": {
    "destination": "/app/chat/typing"
  },
  "body": "{\"conversationId\":1,\"userId\":10,\"typing\":true}"
}
```

### Kết Quả Mong Đợi

Sau khi gửi tin nhắn, bạn sẽ nhận được:

**1. Broadcast Message (tất cả clients trong conversation):**
```json
{
  "id": 42,
  "conversationId": 1,
  "senderId": 10,
  "senderName": "Nguyễn Văn A",
  "senderAvatar": "https://example.com/avatar.jpg",
  "messageType": "TEXT",
  "content": "Xin chào! Đây là tin nhắn test",
  "createdAt": "2025-12-05T14:30:00",
  "status": "SENT"
}
```

**2. Delivery Confirmation (chỉ người gửi):**
```json
{
  "messageId": 42,
  "status": "DELIVERED"
}
```

---

## 2️⃣ Test Bằng Browser Console (Chrome/Edge)

### Bước 1: Chuẩn Bị

1. Mở trình duyệt Chrome/Edge
2. Truy cập: http://localhost:8080 (hoặc bất kỳ trang nào)
3. Nhấn **F12** để mở Developer Tools
4. Chuyển sang tab **Console**

### Bước 2: Load Thư Viện

Paste đoạn code này vào console:

```javascript
// Load SockJS
var script1 = document.createElement('script');
script1.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
document.head.appendChild(script1);

// Load StompJS
var script2 = document.createElement('script');
script2.src = 'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js';
document.head.appendChild(script2);

console.log('✅ Đang tải thư viện... Đợi 3 giây rồi chạy tiếp!');
```

### Bước 3: Kết Nối (Đợi 3 giây sau bước 2)

```javascript
// Thay YOUR_JWT_TOKEN bằng token thật
const token = 'YOUR_JWT_TOKEN';

const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// Kết nối
stompClient.connect(
  { 'Authorization': `Bearer ${token}` },
  function(frame) {
    console.log('✅ Kết nối thành công!', frame);
    
    // Subscribe nhận tin nhắn
    stompClient.subscribe('/topic/conversations/1', function(message) {
      console.log('📩 Nhận được tin nhắn:', JSON.parse(message.body));
    });
    
    // Subscribe typing indicator
    stompClient.subscribe('/topic/conversations/1/typing', function(message) {
      console.log('⌨️ Typing:', JSON.parse(message.body));
    });
    
    // Subscribe confirmation
    stompClient.subscribe('/user/queue/confirmations', function(message) {
      console.log('✔️ Xác nhận:', JSON.parse(message.body));
    });
    
    console.log('✅ Đã subscribe thành công!');
  },
  function(error) {
    console.error('❌ Lỗi kết nối:', error);
  }
);
```

### Bước 4: Gửi Tin Nhắn

```javascript
// Gửi tin nhắn text
stompClient.send('/app/chat/send', {}, JSON.stringify({
  conversationId: 1,
  senderId: 10,
  messageType: 'TEXT',
  content: 'Hello từ browser console!'
}));

// Gửi tin nhắn hình ảnh
stompClient.send('/app/chat/send', {}, JSON.stringify({
  conversationId: 1,
  senderId: 10,
  messageType: 'IMAGE',
  content: 'https://example.com/image.jpg'
}));
```

### Bước 5: Gửi Typing Indicator

```javascript
// Đang gõ
stompClient.send('/app/chat/typing', {}, JSON.stringify({
  conversationId: 1,
  userId: 10,
  typing: true
}));

// Dừng gõ
stompClient.send('/app/chat/typing', {}, JSON.stringify({
  conversationId: 1,
  userId: 10,
  typing: false
}));
```

### Bước 6: Ngắt Kết Nối

```javascript
stompClient.disconnect(function() {
  console.log('👋 Đã ngắt kết nối');
});
```

---

## 3️⃣ Test Bằng React Frontend

### Bước 1: Cài Đặt Thư Viện

```bash
cd d:\4th_Years\PBL6\ecommerce-frontend
npm install @stomp/stompjs sockjs-client
```

### Bước 2: Tạo File Test Component

Tạo file: `src/components/WebSocketTest.jsx`

```javascript
import { useEffect, useState, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function WebSocketTest() {
  const [messages, setMessages] = useState([]);
  const [connected, setConnected] = useState(false);
  const [inputMessage, setInputMessage] = useState('');
  const [typing, setTyping] = useState(null);
  const stompClientRef = useRef(null);

  const conversationId = 1; // Thay bằng conversation ID thật
  const token = localStorage.getItem('jwt_token'); // Lấy token từ localStorage

  useEffect(() => {
    if (!token) {
      console.error('❌ Không có JWT token!');
      return;
    }

    // Tạo STOMP client
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {
        'Authorization': `Bearer ${token}`
      },
      debug: (str) => {
        console.log('STOMP:', str);
      },
      onConnect: () => {
        console.log('✅ Kết nối WebSocket thành công!');
        setConnected(true);

        // Subscribe nhận tin nhắn
        client.subscribe(`/topic/conversations/${conversationId}`, (message) => {
          const newMsg = JSON.parse(message.body);
          console.log('📩 Tin nhắn mới:', newMsg);
          setMessages(prev => [...prev, newMsg]);
        });

        // Subscribe typing
        client.subscribe(`/topic/conversations/${conversationId}/typing`, (message) => {
          const typingData = JSON.parse(message.body);
          console.log('⌨️ Typing:', typingData);
          if (typingData.typing) {
            setTyping(typingData.userName);
            setTimeout(() => setTyping(null), 3000);
          } else {
            setTyping(null);
          }
        });

        // Subscribe confirmation
        client.subscribe('/user/queue/confirmations', (message) => {
          const confirmation = JSON.parse(message.body);
          console.log('✔️ Xác nhận:', confirmation);
        });
      },
      onStompError: (frame) => {
        console.error('❌ Lỗi STOMP:', frame);
        setConnected(false);
      },
      onDisconnect: () => {
        console.log('👋 Đã ngắt kết nối');
        setConnected(false);
      }
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [token, conversationId]);

  const sendMessage = () => {
    if (!stompClientRef.current || !connected || !inputMessage.trim()) return;

    const currentUserId = getUserIdFromToken(token);

    stompClientRef.current.publish({
      destination: '/app/chat/send',
      body: JSON.stringify({
        conversationId: conversationId,
        senderId: currentUserId,
        messageType: 'TEXT',
        content: inputMessage
      })
    });

    setInputMessage('');
  };

  const sendTyping = (isTyping) => {
    if (!stompClientRef.current || !connected) return;

    const currentUserId = getUserIdFromToken(token);

    stompClientRef.current.publish({
      destination: '/app/chat/typing',
      body: JSON.stringify({
        conversationId: conversationId,
        userId: currentUserId,
        typing: isTyping
      })
    });
  };

  const handleInputChange = (e) => {
    setInputMessage(e.target.value);
    sendTyping(true);
    
    // Tự động gửi "stopped typing" sau 2 giây
    clearTimeout(window.typingTimeout);
    window.typingTimeout = setTimeout(() => {
      sendTyping(false);
    }, 2000);
  };

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
      <h1>🧪 WebSocket Chat Test</h1>
      
      <div style={{ 
        padding: '10px', 
        marginBottom: '20px',
        backgroundColor: connected ? '#d4edda' : '#f8d7da',
        border: `1px solid ${connected ? '#c3e6cb' : '#f5c6cb'}`,
        borderRadius: '4px'
      }}>
        {connected ? '✅ Đã kết nối' : '❌ Chưa kết nối'}
      </div>

      {typing && (
        <div style={{ 
          padding: '10px', 
          marginBottom: '10px',
          backgroundColor: '#e7f3ff',
          border: '1px solid #b3d9ff',
          borderRadius: '4px'
        }}>
          ⌨️ {typing} đang gõ...
        </div>
      )}

      <div style={{ 
        height: '300px', 
        overflowY: 'auto', 
        border: '1px solid #ccc',
        padding: '10px',
        marginBottom: '10px',
        backgroundColor: '#f9f9f9'
      }}>
        {messages.map((msg, index) => (
          <div key={index} style={{ 
            marginBottom: '10px',
            padding: '8px',
            backgroundColor: '#fff',
            borderRadius: '4px',
            border: '1px solid #e0e0e0'
          }}>
            <strong>{msg.senderName}:</strong> {msg.content}
            <div style={{ fontSize: '12px', color: '#666' }}>
              {new Date(msg.createdAt).toLocaleTimeString('vi-VN')}
            </div>
          </div>
        ))}
      </div>

      <div style={{ display: 'flex', gap: '10px' }}>
        <input
          type="text"
          value={inputMessage}
          onChange={handleInputChange}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Nhập tin nhắn..."
          style={{ 
            flex: 1, 
            padding: '10px',
            border: '1px solid #ccc',
            borderRadius: '4px'
          }}
          disabled={!connected}
        />
        <button
          onClick={sendMessage}
          disabled={!connected}
          style={{ 
            padding: '10px 20px',
            backgroundColor: connected ? '#007bff' : '#ccc',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: connected ? 'pointer' : 'not-allowed'
          }}
        >
          Gửi
        </button>
      </div>
    </div>
  );
}

function getUserIdFromToken(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return parseInt(payload.sub);
  } catch (e) {
    console.error('❌ Lỗi parse token:', e);
    return null;
  }
}
```

### Bước 3: Thêm Vào App

Sửa file `src/App.jsx`:

```javascript
import WebSocketTest from './components/WebSocketTest';

function App() {
  return (
    <div>
      <WebSocketTest />
    </div>
  );
}

export default App;
```

### Bước 4: Chạy Test

```bash
npm run dev
```

Mở trình duyệt: http://localhost:5173

---

## 4️⃣ Script Test Nhanh - Copy & Paste

### Script 1: Test Kết Nối

```javascript
// === BƯỚC 1: LOAD THỦ VIỆN ===
var script1 = document.createElement('script');
script1.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
document.head.appendChild(script1);
var script2 = document.createElement('script');
script2.src = 'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js';
document.head.appendChild(script2);
console.log('✅ Đợi 3 giây...');

// === BƯỚC 2: KẾT NỐI (sau 3 giây) ===
setTimeout(() => {
  const token = 'YOUR_JWT_TOKEN'; // ⚠️ THAY JWT TOKEN
  const conversationId = 1; // ⚠️ THAY CONVERSATION ID
  
  const socket = new SockJS('http://localhost:8080/ws');
  window.stompClient = Stomp.over(socket);
  
  window.stompClient.connect(
    { 'Authorization': `Bearer ${token}` },
    (frame) => {
      console.log('✅ KẾT NỐI THÀNH CÔNG!');
      
      window.stompClient.subscribe(`/topic/conversations/${conversationId}`, (msg) => {
        console.log('📩 TIN NHẮN:', JSON.parse(msg.body));
      });
      
      window.stompClient.subscribe(`/topic/conversations/${conversationId}/typing`, (msg) => {
        console.log('⌨️ TYPING:', JSON.parse(msg.body));
      });
      
      console.log('✅ SẴN SÀNG! Dùng window.stompClient để gửi tin nhắn');
    },
    (error) => console.error('❌ LỖI:', error)
  );
}, 3000);
```

### Script 2: Gửi Tin Nhắn

```javascript
// Gửi tin nhắn (sau khi đã kết nối ở Script 1)
window.stompClient.send('/app/chat/send', {}, JSON.stringify({
  conversationId: 1, // ⚠️ THAY ID
  senderId: 10,      // ⚠️ THAY USER ID
  messageType: 'TEXT',
  content: 'Test message từ console!'
}));
```

---

## 🔍 Checklist Kiểm Tra

### Trước Khi Test

- [ ] Spring Boot server đang chạy (port 8080)
- [ ] Database có conversation với ID bạn dùng
- [ ] User phải là member của conversation
- [ ] Có JWT token hợp lệ
- [ ] Token chưa hết hạn

### Test Thành Công Khi

- [ ] Kết nối WebSocket không lỗi
- [ ] Subscribe không có error
- [ ] Gửi tin nhắn thấy log trong console
- [ ] Nhận được broadcast message
- [ ] Nhận được delivery confirmation
- [ ] Typing indicator hoạt động

---

## 🐛 Xử Lý Lỗi Thường Gặp

### Lỗi: "Missing or invalid Authorization header"

**Nguyên nhân:** JWT token không đúng  
**Giải pháp:**
1. Kiểm tra token có đúng format: `Bearer <token>`
2. Đảm bảo token chưa hết hạn
3. Test token bằng REST API trước

### Lỗi: "Access Denied"

**Nguyên nhân:** User không phải member của conversation  
**Giải pháp:**
1. Check database `conversation_members`
2. Đảm bảo `user_id` có trong conversation

### Lỗi: "WebSocket connection failed"

**Nguyên nhân:** Server không chạy hoặc CORS  
**Giải pháp:**
1. Kiểm tra server đang chạy: http://localhost:8080
2. Check log server có lỗi gì không
3. Xem `WebSocketConfig.java` đã set CORS đúng chưa

### Lỗi: "senderId mismatch"

**Nguyên nhân:** senderId khác với user trong JWT  
**Giải pháp:**
1. Parse JWT để lấy user ID: `JSON.parse(atob(token.split('.')[1])).sub`
2. Dùng đúng user ID khi gửi tin nhắn

---

## 📊 Kết Quả Mong Đợi

### Console Logs (Thành Công)

```
✅ Kết nối thành công!
✅ Đã subscribe thành công!
📩 Nhận được tin nhắn: {
  id: 42,
  conversationId: 1,
  senderId: 10,
  senderName: "Nguyễn Văn A",
  content: "Hello!",
  createdAt: "2025-12-05T14:30:00"
}
✔️ Xác nhận: { messageId: 42, status: "DELIVERED" }
```

### Database (Sau khi gửi)

**Bảng `messages`:**
```sql
SELECT * FROM messages ORDER BY id DESC LIMIT 1;
-- Sẽ thấy tin nhắn vừa gửi
```

**Bảng `conversations`:**
```sql
SELECT last_activity_at FROM conversations WHERE id = 1;
-- Thời gian phải được cập nhật
```

---

## 🎯 Kịch Bản Test Đầy Đủ

### Test Case 1: Gửi - Nhận Tin Nhắn

1. Mở 2 tab browser
2. Tab 1: Kết nối với user A
3. Tab 2: Kết nối với user B (cùng conversation)
4. Tab 1 gửi tin nhắn
5. **Kết quả:** Tab 2 nhận được tin nhắn

### Test Case 2: Typing Indicator

1. Tab 1 gửi typing = true
2. **Kết quả:** Tab 2 thấy "User A đang gõ..."
3. Tab 1 gửi typing = false
4. **Kết quả:** Tab 2 không thấy typing nữa

### Test Case 3: Delivery Confirmation

1. Tab 1 gửi tin nhắn
2. **Kết quả:** Tab 1 nhận confirmation qua `/user/queue/confirmations`

### Test Case 4: Permission Check

1. User C (không phải member) kết nối
2. User C gửi tin nhắn vào conversation
3. **Kết quả:** Server trả lỗi "Access Denied"

---

## 🚀 Bắt Đầu Test Ngay!

### Cách Nhanh Nhất (30 giây)

1. Mở Chrome DevTools (F12)
2. Copy Script 1 vào console
3. Đợi 3 giây
4. Thay JWT token
5. Copy Script 2 để gửi tin nhắn
6. ✅ Xong!

### Cách Chuyên Nghiệp (5 phút)

1. Mở Postman
2. Tạo WebSocket Request
3. Connect với JWT token
4. Subscribe topics
5. Gửi tin nhắn
6. Kiểm tra response

**Chúc bạn test thành công! 🎉**
