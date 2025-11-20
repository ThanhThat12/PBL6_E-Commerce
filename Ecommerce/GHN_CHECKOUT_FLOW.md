# Flow Checkout GHN Chuẩn - Đã Hoàn Thành

## 📋 Tổng Quan

**Flow chuẩn thương mại điện tử:**

1. Buyer đặt hàng → Tạo Order (PENDING) - **CHƯA tạo vận đơn GHN**
2. Seller xác nhận → Tạo Shipment + Gọi GHN API → Order chuyển PROCESSING

---

## 🔄 Flow Chi Tiết

### **Bước 1-2: Buyer chọn dịch vụ vận chuyển**

**Frontend:** PaymentPage.jsx

- User chọn địa chỉ giao hàng
- Gọi `POST /api/checkout/available-services` để lấy danh sách dịch vụ GHN
- Hiển thị dropdown dịch vụ cho mỗi shop

**Backend:** CheckoutController.java

```java
POST /api/checkout/available-services
Body: { shopId, addressId }
Response: List<GhnServiceDTO>
```

---

### **Bước 3: Buyer chọn service → Tính phí ship**

**Frontend:** PaymentPage.jsx

- User chọn service từ dropdown
- Gọi `POST /api/checkout/calculate-fee`

**Backend:** CheckoutController.java

```java
POST /api/checkout/calculate-fee
Body: { shopId, addressId, serviceId, serviceTypeId, cartItemIds[] }
Response: { shippingFee, expectedDeliveryTime, ... }
```

---

### **Bước 4: Buyer xác nhận đặt hàng**

**Frontend:** PaymentPage.jsx

```javascript
const confirmData = {
  shopId: parseInt(shopId),
  addressId: parseInt(shippingAddress.id),
  serviceId: parseInt(selectedService.service_id),
  serviceTypeId: parseInt(selectedService.service_type_id),
  cartItemIds: cartItemIds.map((id) => parseInt(id)),
  paymentMethod: paymentMethod, // COD, MOMO, SPORTYPAY
  note: orderNotes || "",
};

await api.post("/api/checkout/confirm", confirmData);
```

**Backend:** CheckoutController.java

```java
POST /api/checkout/confirm
Body: CheckoutConfirmRequestDTO
Actions:
  1. Tạo Order (status=PENDING, paymentStatus=UNPAID)
  2. Lưu GHN service info vào order.notes (JSON format)
  3. Tạo OrderItems
  4. Xóa CartItems
Response: { orderId, totalAmount, status, message }
```

**Lưu ý:**

- Order được tạo với status=PENDING
- **CHƯA tạo Shipment, CHƯA gọi GHN API**
- Thông tin GHN service được lưu tạm trong `order.notes`:

```json
{
  "serviceId": 53320,
  "serviceTypeId": 2,
  "addressId": 123,
  "note": "Giao ngoài giờ hành chính"
}
```

---

### **Bước 5: Seller xác nhận đơn hàng và ship**

**Frontend:** Seller Dashboard (cần tạo)

```javascript
await api.post(`/api/seller/orders/${orderId}/confirm-and-ship`);
```

**Backend:** SellerOrderController.java (MỚI)

```java
POST /api/seller/orders/{orderId}/confirm-and-ship
Authorization: ROLE_SELLER
Actions:
  1. Kiểm tra order thuộc shop của seller
  2. Kiểm tra order.status == PENDING
  3. Parse GHN service info từ order.notes
  4. Lấy địa chỉ buyer và shop
  5. Chuẩn bị payload GHN (từ, đến, items, COD...)
  6. Gọi GHN API: createShippingOrder()
  7. Tạo Shipment trong DB
  8. Cập nhật Order: status=PROCESSING, shipment_id=...
Response: { orderId, shipmentId, ghnOrderCode, status, shippingFee }
```

---

## 📂 Files Đã Tạo/Sửa

### **Backend:**

1. **CheckoutController.java**

   - `POST /api/checkout/available-services` - Lấy danh sách dịch vụ GHN
   - `POST /api/checkout/calculate-fee` - Tính phí ship
   - `POST /api/checkout/confirm` - Tạo order (KHÔNG tạo shipment)

2. **SellerOrderController.java** ⭐ **MỚI**

   - `POST /api/seller/orders/{orderId}/confirm-and-ship` - Seller xác nhận + tạo GHN shipment

3. **Order.java**
   - Đã có sẵn field `notes` để lưu GHN service info

### **Frontend:**

1. **PaymentPage.jsx**

   - State: `ghnServices`, `selectedServices`, `shopShippingFees`
   - `fetchAvailableServices()` - Gọi khi chọn địa chỉ
   - `handleServiceSelect()` - Gọi calculate-fee khi chọn service
   - `handlePlaceOrder()` - Gọi `/api/checkout/confirm` (không tạo shipment)
   - UI: Dropdown chọn service cho mỗi shop

2. **Seller Dashboard** (Cần tạo)
   - Hiển thị danh sách đơn hàng PENDING
   - Nút "Xác nhận và giao hàng" → Gọi `/api/seller/orders/{orderId}/confirm-and-ship`

---

## 🗄️ Database Schema

### **Bảng `orders`**

```sql
CREATE TABLE `orders` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `shop_id` bigint,
  `shipment_id` bigint, -- NULL khi mới tạo, có giá trị sau khi seller xác nhận
  `total_amount` decimal(15,2),
  `method` enum('COD','MOMO','SPORTYPAY'),
  `status` enum('PENDING','PROCESSING','SHIPPING','COMPLETED','CANCELLED'),
  `payment_status` enum('UNPAID','PAID','FAILED'),
  `notes` TEXT, -- Lưu JSON: { serviceId, serviceTypeId, addressId, note }
  `created_at` datetime,
  `updated_at` datetime
);
```

### **Bảng `shipments`**

```sql
CREATE TABLE `shipments` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `ghn_order_code` varchar(100),
  `shipping_fee` decimal(10,2),
  `status` enum('PENDING','READY_TO_PICK','PICKED_UP','IN_TRANSIT','DELIVERED','CANCELLED'),
  `receiver_name` varchar(255),
  `receiver_phone` varchar(20),
  `receiver_address` text,
  `province` varchar(100),
  `district` varchar(100),
  `ward` varchar(100),
  `expected_delivery_time` varchar(255),
  `ghn_payload` TEXT, -- Lưu toàn bộ GHN response
  `created_at` datetime,
  `updated_at` datetime
);
```

---

## 🚀 Cách Test

### **1. Test Flow Buyer**

```bash
# Bước 1: Lấy danh sách dịch vụ GHN
POST http://localhost:8080/api/checkout/available-services
{
  "shopId": 1,
  "addressId": 10
}

# Bước 2: Tính phí ship
POST http://localhost:8080/api/checkout/calculate-fee
{
  "shopId": 1,
  "addressId": 10,
  "serviceId": 53320,
  "serviceTypeId": 2,
  "cartItemIds": [1, 2, 3]
}

# Bước 3: Đặt hàng
POST http://localhost:8080/api/checkout/confirm
Authorization: Bearer <buyer_token>
{
  "shopId": 1,
  "addressId": 10,
  "serviceId": 53320,
  "serviceTypeId": 2,
  "cartItemIds": [1, 2, 3],
  "paymentMethod": "COD",
  "note": "Giao ngoài giờ hành chính"
}

# Response: { "orderId": 123, "status": "PENDING", ... }
```

### **2. Test Flow Seller**

```bash
# Seller xác nhận đơn hàng và tạo vận đơn GHN
POST http://localhost:8080/api/seller/orders/123/confirm-and-ship
Authorization: Bearer <seller_token>

# Response:
{
  "orderId": 123,
  "shipmentId": 456,
  "ghnOrderCode": "LMAE0FBC",
  "status": "PROCESSING",
  "shippingFee": 35000
}
```

### **3. Kiểm tra DB**

```sql
-- Kiểm tra order sau khi buyer đặt hàng
SELECT id, status, shipment_id, notes FROM orders WHERE id = 123;
-- shipment_id = NULL, status = PENDING

-- Kiểm tra order sau khi seller xác nhận
SELECT id, status, shipment_id FROM orders WHERE id = 123;
-- shipment_id = 456, status = PROCESSING

-- Kiểm tra shipment
SELECT id, ghn_order_code, shipping_fee, status FROM shipments WHERE id = 456;
```

---

## ✅ Ưu Điểm Flow Này

1. **Đúng chuẩn thương mại điện tử:**

   - Buyer đặt hàng → Seller xác nhận → Mới tạo vận đơn

2. **Tiết kiệm chi phí:**

   - Không tạo vận đơn GHN nếu seller hủy/từ chối đơn

3. **Linh hoạt:**

   - Seller có thể kiểm tra hàng tồn kho trước khi xác nhận
   - Có thể hủy đơn trước khi tạo vận đơn GHN

4. **Tách biệt rõ ràng:**
   - Checkout logic (buyer) ≠ Shipping logic (seller)
   - Dễ maintain và mở rộng

---

## 📝 TODO - Cần Làm Thêm

### **Frontend:**

- [ ] Tạo Seller Dashboard page
- [ ] Hiển thị danh sách đơn hàng PENDING
- [ ] Button "Xác nhận và giao hàng"
- [ ] Hiển thị thông tin shipment sau khi tạo

### **Backend:**

- [ ] API lấy danh sách đơn hàng PENDING của seller
- [ ] Webhook GHN để cập nhật trạng thái shipment
- [ ] Logic hủy đơn hàng PENDING
- [ ] Notification cho buyer khi seller xác nhận

---

## 🎯 Kết Luận

✅ **Backend:** Hoàn thành
✅ **Frontend Payment:** Hoàn thành
⏳ **Seller Dashboard:** Chưa làm (cần tạo UI)

**Next Steps:**

1. Test flow đầy đủ: Buyer đặt hàng → Seller xác nhận
2. Tạo Seller Dashboard UI
3. Thêm webhook GHN để cập nhật trạng thái tự động
