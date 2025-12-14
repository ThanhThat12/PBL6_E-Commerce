# Tổng Hợp Thay Đổi: Hệ Thống Thông Báo

## Ngày: 14/12/2025

---

## 1. Sửa Lỗi Notification System

### 1.1 Fix Notification Type Filter

**Vấn đề**: Backend gửi `NEW_ORDER` nhưng frontend filter `ORDER_PLACED`

**Giải pháp**:

- ✅ Thêm `NEW_ORDER`, `PAYMENT_RECEIVED`, `ORDER_COMPLETED` vào seller notification filter
- File: `src/hooks/useNotifications.js`

```javascript
if (role === "SELLER") {
  const match =
    n.type === "NEW_ORDER" ||
    n.type === "ORDER_PLACED" ||
    n.type === "ORDER_CANCELLED" ||
    n.type === "PAYMENT_RECEIVED" ||
    n.type === "ORDER_COMPLETED";
  return match;
}
```

### 1.2 Fix Notification Click Navigation

**Vấn đề**: Click notification không navigate đến order detail

**Giải pháp**:

- ✅ Buyer: Navigate trực tiếp đến `/orders/{orderId}`
- ✅ Seller: Navigate đến `/seller/orders?orderId={orderId}` → auto-open modal
- ✅ Extract orderId từ message nếu notification.orderId = null

**Files thay đổi**:

- `src/components/common/Navbar/NotificationButton.jsx`
- `src/components/seller/Layout/NotificationDropdown.jsx`
- `src/pages/seller/Orders.jsx`

**Code quan trọng**:

```javascript
// NotificationButton.jsx - Buyer
let orderId = notification.orderId;
if (!orderId && notification.message) {
  const match = notification.message.match(/#(\d+)/);
  if (match) {
    orderId = parseInt(match[1]);
  }
}
if (orderId) {
  navigate(`/orders/${orderId}`);
}

// NotificationDropdown.jsx - Seller
navigate(`/seller/orders?orderId=${notification.orderId}`);

// Orders.jsx - Seller auto-open modal
useEffect(() => {
  const orderIdParam = searchParams.get("orderId");
  if (orderIdParam) {
    const orderId = parseInt(orderIdParam);
    if (!isNaN(orderId)) {
      handleViewDetail(orderId);
      setSearchParams({});
    }
  }
}, [searchParams, setSearchParams]);
```

### 1.3 Fix Notification Read Status

**Vấn đề**: Backend dùng `isRead`, frontend dùng `read` → reload page vẫn hiển thị unread

**Giải pháp**:

- ✅ Update tất cả chỗ frontend để check cả `isRead` và `read`
- File: `src/hooks/useNotifications.js`

```javascript
// Mark as read
prev.map((n) =>
  n.id === notificationId ? { ...n, isRead: true, read: true } : n
);

// Count unread
filteredData.filter((n) => !n.isRead && !n.read).length;

// Check new notification
if (!notification.isRead && !notification.read) {
  setUnreadCount((prev) => prev + 1);
}
```

---

## 2. Admin Notification System

### 2.1 Add Admin Role Support

**Backend**: `NotificationService.java`

```java
public void sendAdminNotification(String type, String message, Long orderId) {
    List<User> admins = userRepository.findByRole(Role.ADMIN);
    if (admins.isEmpty()) return;

    User admin = admins.get(0); // Chỉ có 1 admin

    // Lưu vào DB
    Notification notification = new Notification();
    notification.setUser(admin);
    notification.setType(type);
    notification.setMessage(message);
    notification.setOrderId(orderId);
    notification.setIsRead(false);
    notification.setCreatedAt(LocalDateTime.now());

    Notification savedNotification = notificationRepository.save(notification);

    // Gửi WebSocket
    String destination = "/topic/admin/" + admin.getId();
    Map<String, Object> notificationData = new HashMap<>();
    notificationData.put("id", savedNotification.getId());
    notificationData.put("type", savedNotification.getType());
    notificationData.put("message", savedNotification.getMessage());
    notificationData.put("orderId", savedNotification.getOrderId());
    notificationData.put("read", savedNotification.getIsRead());
    notificationData.put("createdAt", savedNotification.getCreatedAt());

    messagingTemplate.convertAndSend(destination, notificationData);
}
```

### 2.2 Frontend Admin Support

**File**: `src/context/NotificationContext.jsx`

```javascript
const role = useMemo(() => {
  if (location.pathname.startsWith("/admin")) {
    return "ADMIN";
  }
  if (location.pathname.startsWith("/seller")) {
    return "SELLER";
  }
  return "BUYER";
}, [location.pathname]);
```

**File**: `src/hooks/useNotifications.js`

```javascript
if (role === "ADMIN") {
  // Admin thấy: NEW_ORDER, PAYMENT_RECEIVED, SELLER_PAYOUT, SELLER_REGISTRATION
  const match =
    n.type === "NEW_ORDER" ||
    n.type === "PAYMENT_RECEIVED" ||
    n.type === "SELLER_PAYOUT" ||
    n.type === "SELLER_REGISTRATION";
  return match;
}

// WebSocket channel mapping
const channelMap = {
  BUYER: `/topic/orderws/${userId}`,
  SELLER: `/topic/sellerws/${userId}`,
  ADMIN: `/topic/admin/${userId}`,
};
```

### 2.3 Admin Notification Navigation

**File**: `src/components/common/Navbar/NotificationButton.jsx`

```javascript
if (variant === "admin") {
  switch (notification.type) {
    case "SELLER_REGISTRATION":
      navigate("/admin/seller-registrations");
      break;
    case "NEW_ORDER":
    case "PAYMENT_RECEIVED":
    case "SELLER_PAYOUT":
      navigate("/admin/orders");
      break;
  }
}
```

---

## 3. Admin Notification Types

### 3.1 New Order Notification

**File**: `CheckoutController.java`

```java
// Gửi cho admin
String shopName = shop.getName() != null ? shop.getName() : "Shop #" + shop.getId();
String customerName = user.getUsername() != null ? user.getUsername() : "Khách hàng #" + user.getId();
String adminMessage = "Có đơn hàng #" + order.getId() + " của shop " + shopName + " từ khách hàng " + customerName;
notificationService.sendAdminNotification("NEW_ORDER", adminMessage, order.getId());
```

### 3.2 Payment Received & Seller Payout Notification

**File**: `SellerPayoutScheduler.java`

```java
// Admin nhận commission (10%)
String customerName = order.getUser().getUsername() != null ?
                     order.getUser().getUsername() : "Khách hàng #" + order.getUser().getId();
String adminMessage = String.format("Bạn đã nhận được %.0f VNĐ của đơn hàng #%d từ khách hàng %s",
                                   adminCommission, order.getId(), customerName);
notificationService.sendAdminNotification("PAYMENT_RECEIVED", adminMessage, order.getId());

// Admin notification về seller payout
String shopName = order.getShop().getName() != null ?
                 order.getShop().getName() : "Shop #" + order.getShop().getId();
String sellerPayoutMessage = String.format("Đã chuyển %.0f VNĐ về ví của shop %s cho đơn hàng #%d",
                                          sellerAmount, shopName, order.getId());
notificationService.sendAdminNotification("SELLER_PAYOUT", sellerPayoutMessage, order.getId());
```

### 3.3 Seller Registration Notification

**File**: `SellerRegistrationService.java`

```java
// Gửi thông báo cho admin khi có buyer đăng ký seller
String userName = user.getUsername() != null ? user.getUsername() : "User #" + user.getId();
String adminMessage = String.format("Có đơn đăng ký seller mới từ %s - Shop: %s",
                                   userName, savedShop.getName());
notificationService.sendAdminNotification("SELLER_REGISTRATION", adminMessage, null);
```

---

## 4. Auto Cleanup Notifications

### 4.1 NotificationCleanupScheduler

**File**: `NotificationCleanupScheduler.java`

```java
@Component
public class NotificationCleanupScheduler {

    private static final int CLEANUP_MINUTES = 5;
    private final NotificationRepository notificationRepository;

    @Scheduled(fixedRate = 60000) // Chạy mỗi 1 phút
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(CLEANUP_MINUTES);

        // Xóa các thông báo đã đọc và cũ hơn 5 phút
        int deletedCount = notificationRepository
            .deleteByIsReadTrueAndCreatedAtBefore(cutoffTime);

        if (deletedCount > 0) {
            logger.info("🗑️ Cleaned up {} old read notifications", deletedCount);
        }
    }
}
```

### 4.2 Repository Method

**File**: `NotificationRepository.java`

```java
@Modifying
@Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :cutoffTime")
int deleteByIsReadTrueAndCreatedAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
```

---

## 5. Per-Item Refund Feature

### 5.1 ReturnRequestPage

**File**: `src/pages/buyer/ReturnRequestPage.jsx`

**Tính năng**:

- ✅ Select quantity to return (1 to item.quantity)
- ✅ Upload refund images (max 5)
- ✅ Calculate refund amount: `item.price * returnQuantity`
- ✅ Submit refund request to backend

```javascript
const [returnQuantity, setReturnQuantity] = useState(1);
const refundAmount = selectedItem.price * returnQuantity;

<input
  type="number"
  min="1"
  max={selectedItem.quantity}
  value={returnQuantity}
  onChange={(e) =>
    setReturnQuantity(
      Math.min(
        selectedItem.quantity,
        Math.max(1, parseInt(e.target.value) || 1)
      )
    )
  }
/>;
```

### 5.2 Add Button to OrderDetailPage

**File**: `src/pages/buyer/OrderDetailPage.jsx`

```javascript
{
  order.status === "COMPLETED" && (
    <button
      onClick={() =>
        navigate(`/orders/return?orderId=${orderId}&itemId=${item.id}`)
      }
      className="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600"
    >
      Trả hàng/Hoàn tiền
    </button>
  );
}
```

---

## 6. Bug Fixes

### 6.1 Fixed handleViewDetail Not Defined

**File**: `src/pages/seller/Orders.jsx`

```javascript
const handleViewDetail = async (orderId) => {
  try {
    setDetailLoading(true);
    const detail = await getOrderDetail(orderId);
    setSelectedOrder(detail);
    setDetailModalVisible(true);
  } catch (err) {
    console.error("Error fetching order detail:", err);
    message.error("Không thể tải chi tiết đơn hàng");
  } finally {
    setDetailLoading(false);
  }
};
```

### 6.2 Fixed NotificationDropdown JSX Syntax Error

- Removed orphan `</Link>` tag

---

## 7. Testing Checklist

### Backend

- [ ] Admin nhận notification khi có đơn hàng mới
- [ ] Admin nhận notification khi nhận commission
- [ ] Admin nhận notification khi chuyển tiền cho seller
- [ ] Admin nhận notification khi có buyer đăng ký seller
- [ ] Notification tự động xóa sau 5 phút khi đã đọc

### Frontend - Buyer

- [ ] Click notification navigate đến order detail page
- [ ] Extract orderId từ message nếu notification.orderId = null
- [ ] Mark as read persist sau reload page
- [ ] Per-item refund button hiển thị cho COMPLETED orders
- [ ] Quantity selector hoạt động đúng

### Frontend - Seller

- [ ] Click notification navigate với query param
- [ ] Modal tự động mở từ query param
- [ ] Notification filter đúng (NEW_ORDER, PAYMENT_RECEIVED, etc.)

### Frontend - Admin

- [ ] Admin role được detect đúng từ pathname `/admin`
- [ ] Subscribe đến `/topic/admin/{adminId}`
- [ ] Filter đúng notification types
- [ ] Click SELLER_REGISTRATION → `/admin/seller-registrations`
- [ ] Click order notifications → `/admin/orders`

---

## 8. Files Đã Thay Đổi

### Backend

1. `NotificationService.java` - Added sendAdminNotification()
2. `CheckoutController.java` - Send admin notification on new order
3. `SellerPayoutScheduler.java` - Send admin notifications on payment
4. `SellerRegistrationService.java` - Send admin notification on seller registration
5. `NotificationCleanupScheduler.java` - New file
6. `NotificationRepository.java` - Added deleteByIsReadTrueAndCreatedAtBefore()

### Frontend

1. `src/hooks/useNotifications.js` - Fixed filters, read status, admin support
2. `src/context/NotificationContext.jsx` - Detect admin role
3. `src/components/common/Navbar/NotificationButton.jsx` - Admin navigation, extract orderId
4. `src/components/seller/Layout/NotificationDropdown.jsx` - Query param navigation
5. `src/pages/seller/Orders.jsx` - Added handleViewDetail, auto-open modal
6. `src/pages/buyer/ReturnRequestPage.jsx` - New file
7. `src/pages/buyer/OrderDetailPage.jsx` - Added refund button

---

## 9. WebSocket Channels

| Role   | Channel                    | Notification Types                                                          |
| ------ | -------------------------- | --------------------------------------------------------------------------- |
| BUYER  | `/topic/orderws/{userId}`  | ORDER_CONFIRMED, ORDER_SHIPPING, ORDER_DELIVERED, ORDER_CANCELLED           |
| SELLER | `/topic/sellerws/{userId}` | NEW_ORDER, ORDER_PLACED, ORDER_CANCELLED, PAYMENT_RECEIVED, ORDER_COMPLETED |
| ADMIN  | `/topic/admin/{userId}`    | NEW_ORDER, PAYMENT_RECEIVED, SELLER_PAYOUT, SELLER_REGISTRATION             |

---

## 10. Database Schema

### Notification Table

```sql
CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(50) NOT NULL,
  message TEXT NOT NULL,
  order_id BIGINT NULL,
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,

  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Notification Types

- `NEW_ORDER` - Đơn hàng mới
- `ORDER_PLACED` - Đơn hàng đã đặt
- `ORDER_CONFIRMED` - Đơn hàng đã xác nhận
- `ORDER_SHIPPING` - Đơn hàng đang giao
- `ORDER_COMPLETED` - Đơn hàng hoàn thành
- `ORDER_CANCELLED` - Đơn hàng đã hủy
- `PAYMENT_RECEIVED` - Đã nhận thanh toán
- `SELLER_PAYOUT` - Đã chuyển tiền cho seller
- `SELLER_REGISTRATION` - Đăng ký seller mới
- `RETURN_REQUESTED` - Yêu cầu trả hàng

---

## 11. Commission Flow

1. **Buyer thanh toán** → Tiền vào admin wallet
2. **Sau 2 phút** → Scheduler tự động chuyển:
   - Admin giữ 10% commission
   - Seller nhận 90%
3. **Admin nhận 2 notifications**:
   - `PAYMENT_RECEIVED`: "Bạn đã nhận được {commission} của đơn hàng #{id}"
   - `SELLER_PAYOUT`: "Đã chuyển {amount} về ví của shop {name}"

---

## Kết Luận

Hệ thống notification đã được hoàn thiện với:

- ✅ Notification persistence vào DB
- ✅ Role-based filtering (BUYER, SELLER, ADMIN)
- ✅ WebSocket real-time delivery
- ✅ Click navigation đến page tương ứng
- ✅ Admin monitoring (orders, payments, registrations)
- ✅ Auto cleanup sau 5 phút
- ✅ Per-item refund với quantity selector
- ✅ Read status sync giữa frontend và backend

**Tổng số files thay đổi**: 13 files (7 backend + 6 frontend)
