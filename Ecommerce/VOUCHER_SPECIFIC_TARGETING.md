# Hướng dẫn tạo Voucher cho sản phẩm cụ thể và khách hàng cụ thể

## Tổng quan
Hệ thống voucher đã hỗ trợ đầy đủ các chức năng:
- ✅ Tạo voucher áp dụng cho **sản phẩm cụ thể** của shop
- ✅ Tạo voucher áp dụng cho **khách hàng cụ thể** đã mua hàng
- ✅ Tạo voucher cho **Top Buyers** (khách hàng chi tiêu nhiều nhất)
- ✅ Lấy danh sách khách hàng đã từng mua hàng của shop

---

## 1. API: Tạo Voucher

### Endpoint
```
POST /api/seller/vouchers
```

### Request Body
```json
{
  "code": "SUMMER2024",
  "description": "Giảm giá mùa hè",
  "discountType": "PERCENTAGE",
  "discountValue": 20.0,
  "minOrderValue": 100000.0,
  "maxDiscountAmount": 50000.0,
  "startDate": "2024-06-01T00:00:00",
  "endDate": "2024-08-31T23:59:59",
  "usageLimit": 100,
  "applicableType": "SPECIFIC_PRODUCTS",
  "productIds": [1, 2, 3],
  "userIds": null,
  "topBuyersCount": null
}
```

### Các loại `applicableType`:

#### 1️⃣ **ALL** - Áp dụng cho tất cả sản phẩm
```json
{
  "applicableType": "ALL",
  "productIds": null,
  "userIds": null,
  "topBuyersCount": null
}
```

#### 2️⃣ **SPECIFIC_PRODUCTS** - Áp dụng cho sản phẩm cụ thể
```json
{
  "applicableType": "SPECIFIC_PRODUCTS",
  "productIds": [101, 102, 103],
  "userIds": null,
  "topBuyersCount": null
}
```

**Lưu ý:**
- Hệ thống sẽ **tự động validate** các `productIds` có thuộc shop của seller hay không
- Nếu có sản phẩm không thuộc shop, API trả về lỗi: `"Sản phẩm {id} không thuộc shop của bạn"`

#### 3️⃣ **SPECIFIC_USERS** - Áp dụng cho khách hàng cụ thể
```json
{
  "applicableType": "SPECIFIC_USERS",
  "productIds": null,
  "userIds": [51, 52, 53],
  "topBuyersCount": null
}
```

**Lưu ý:**
- Chỉ áp dụng cho các `userIds` được chỉ định
- Khách hàng khác không thể sử dụng voucher này

#### 4️⃣ **TOP_BUYERS** - Tự động chọn Top khách hàng chi tiêu nhiều nhất
```json
{
  "applicableType": "TOP_BUYERS",
  "productIds": null,
  "userIds": null,
  "topBuyersCount": 10
}
```

**Lưu ý:**
- Hệ thống sẽ tự động truy vấn Top N khách hàng có tổng chi tiêu cao nhất tại shop
- Chỉ những khách hàng này mới được sử dụng voucher

---

## 2. API: Lấy danh sách khách hàng đã mua hàng

### Endpoint (MỚI)
```
GET /api/seller/vouchers/customers
```

### Headers
```
Authorization: Bearer <seller_token>
```

### Response
```json
{
  "code": 200,
  "message": "Lấy danh sách khách hàng thành công",
  "data": [
    {
      "id": 51,
      "username": "buyer1",
      "email": "buyer1@example.com",
      "fullName": "Nguyễn Văn A",
      "phone": "0901234567",
      "orderCount": 15,
      "totalSpent": 5000000.0
    },
    {
      "id": 52,
      "username": "buyer2",
      "email": "buyer2@example.com",
      "fullName": "Trần Thị B",
      "phone": "0912345678",
      "orderCount": 8,
      "totalSpent": 2500000.0
    }
  ]
}
```

**Giải thích:**
- `orderCount`: Tổng số đơn hàng **COMPLETED** của khách tại shop
- `totalSpent`: Tổng số tiền đã chi tiêu (chỉ tính đơn COMPLETED)
- Chỉ trả về khách hàng có ít nhất 1 đơn hàng **COMPLETED**

---

## 3. Luồng xử lý Backend

### A. Khi tạo Voucher với `SPECIFIC_PRODUCTS`
```java
// VoucherService.java - lines 138-152
if (ApplicableType.SPECIFIC_PRODUCTS.equals(request.getApplicableType())) {
    if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
        throw new RuntimeException("Phải chọn ít nhất một sản phẩm");
    }
    
    // Validate từng product có thuộc shop không
    for (Long productId : request.getProductIds()) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm " + productId));
        
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Sản phẩm " + productId + " không thuộc shop của bạn");
        }
        
        // Lưu vào junction table voucher_product
        VoucherProduct voucherProduct = new VoucherProduct();
        voucherProduct.setVoucher(voucher);
        voucherProduct.setProduct(product);
        voucherProductRepository.save(voucherProduct);
    }
}
```

### B. Khi tạo Voucher với `SPECIFIC_USERS`
```java
// VoucherService.java - lines 155-167
if (ApplicableType.SPECIFIC_USERS.equals(request.getApplicableType())) {
    if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
        throw new RuntimeException("Phải chọn ít nhất một khách hàng");
    }
    
    for (Long userId : request.getUserIds()) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user " + userId));
        
        // Lưu vào junction table voucher_user
        VoucherUser voucherUser = new VoucherUser();
        voucherUser.setVoucher(voucher);
        voucherUser.setUser(user);
        voucherUserRepository.save(voucherUser);
    }
}
```

### C. Khi tạo Voucher với `TOP_BUYERS`
```java
// VoucherService.java - lines 170-186
if (ApplicableType.TOP_BUYERS.equals(request.getApplicableType())) {
    if (request.getTopBuyersCount() == null || request.getTopBuyersCount() <= 0) {
        throw new RuntimeException("Số lượng top buyers phải lớn hơn 0");
    }
    
    // Tự động truy vấn top buyers
    List<TopBuyerDTO> topBuyers = orderRepository
        .findTopBuyersByShopWithLimit(shop.getId(), PageRequest.of(0, request.getTopBuyersCount()));
    
    if (topBuyers.isEmpty()) {
        throw new RuntimeException("Không tìm thấy khách hàng nào");
    }
    
    // Lưu vào voucher_user cho từng top buyer
    for (TopBuyerDTO buyer : topBuyers) {
        User user = userRepository.findById(buyer.getUserId()).orElse(null);
        if (user != null) {
            VoucherUser voucherUser = new VoucherUser();
            voucherUser.setVoucher(voucher);
            voucherUser.setUser(user);
            voucherUserRepository.save(voucherUser);
        }
    }
}
```

---

## 4. Cấu trúc Database

### Table: `vouchers`
```sql
CREATE TABLE vouchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    shop_id BIGINT NOT NULL,
    applicable_type ENUM('ALL', 'SPECIFIC_PRODUCTS', 'SPECIFIC_USERS', 'TOP_BUYERS'),
    discount_type ENUM('PERCENTAGE', 'FIXED'),
    discount_value DECIMAL(10,2),
    ...
);
```

### Table: `voucher_product` (Junction Table)
```sql
CREATE TABLE voucher_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    voucher_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

### Table: `voucher_user` (Junction Table)
```sql
CREATE TABLE voucher_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    voucher_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 5. Test các API

### Test 1: Tạo voucher cho sản phẩm ID = 1, 2, 3
```bash
curl -X POST http://localhost:8080/api/seller/vouchers \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "PRODUCT_SALE",
    "description": "Giảm giá cho 3 sản phẩm",
    "discountType": "PERCENTAGE",
    "discountValue": 15.0,
    "minOrderValue": 50000.0,
    "maxDiscountAmount": 30000.0,
    "startDate": "2024-05-01T00:00:00",
    "endDate": "2024-12-31T23:59:59",
    "usageLimit": 50,
    "applicableType": "SPECIFIC_PRODUCTS",
    "productIds": [1, 2, 3]
  }'
```

### Test 2: Lấy danh sách khách hàng đã mua hàng
```bash
curl -X GET http://localhost:8080/api/seller/vouchers/customers \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test 3: Tạo voucher cho khách hàng ID = 51, 52
```bash
curl -X POST http://localhost:8080/api/seller/vouchers \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "VIP_CUSTOMER",
    "description": "Ưu đãi khách hàng VIP",
    "discountType": "FIXED",
    "discountValue": 100000.0,
    "minOrderValue": 500000.0,
    "startDate": "2024-06-01T00:00:00",
    "endDate": "2024-08-31T23:59:59",
    "usageLimit": 5,
    "applicableType": "SPECIFIC_USERS",
    "userIds": [51, 52]
  }'
```

### Test 4: Tạo voucher cho Top 10 khách hàng chi tiêu nhiều nhất
```bash
curl -X POST http://localhost:8080/api/seller/vouchers \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "TOP_BUYER_2024",
    "description": "Ưu đãi Top 10 khách hàng",
    "discountType": "PERCENTAGE",
    "discountValue": 25.0,
    "maxDiscountAmount": 200000.0,
    "startDate": "2024-07-01T00:00:00",
    "endDate": "2024-09-30T23:59:59",
    "usageLimit": 20,
    "applicableType": "TOP_BUYERS",
    "topBuyersCount": 10
  }'
```

---

## 6. Frontend Integration (Gợi ý)

### A. Component tạo Voucher
```jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';

function CreateVoucherForm() {
  const [applicableType, setApplicableType] = useState('ALL');
  const [products, setProducts] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [selectedProductIds, setSelectedProductIds] = useState([]);
  const [selectedUserIds, setSelectedUserIds] = useState([]);

  // Load danh sách khách hàng khi chọn SPECIFIC_USERS
  useEffect(() => {
    if (applicableType === 'SPECIFIC_USERS') {
      axios.get('/api/seller/vouchers/customers', {
        headers: { Authorization: `Bearer ${token}` }
      }).then(res => {
        setCustomers(res.data.data);
      });
    }
  }, [applicableType]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const voucherData = {
      code: 'SUMMER2024',
      discountType: 'PERCENTAGE',
      discountValue: 20.0,
      applicableType: applicableType,
      productIds: applicableType === 'SPECIFIC_PRODUCTS' ? selectedProductIds : null,
      userIds: applicableType === 'SPECIFIC_USERS' ? selectedUserIds : null,
      topBuyersCount: applicableType === 'TOP_BUYERS' ? 10 : null,
      // ... other fields
    };

    await axios.post('/api/seller/vouchers', voucherData);
  };

  return (
    <form onSubmit={handleSubmit}>
      <select value={applicableType} onChange={e => setApplicableType(e.target.value)}>
        <option value="ALL">Tất cả sản phẩm</option>
        <option value="SPECIFIC_PRODUCTS">Sản phẩm cụ thể</option>
        <option value="SPECIFIC_USERS">Khách hàng cụ thể</option>
        <option value="TOP_BUYERS">Top Buyers</option>
      </select>

      {applicableType === 'SPECIFIC_USERS' && (
        <div>
          <h4>Chọn khách hàng (đã từng mua hàng)</h4>
          {customers.map(customer => (
            <label key={customer.id}>
              <input 
                type="checkbox" 
                value={customer.id}
                onChange={e => {
                  if (e.target.checked) {
                    setSelectedUserIds([...selectedUserIds, customer.id]);
                  } else {
                    setSelectedUserIds(selectedUserIds.filter(id => id !== customer.id));
                  }
                }}
              />
              {customer.fullName} - {customer.email} 
              (Đã mua: {customer.orderCount} đơn, 
              Tổng chi: {customer.totalSpent.toLocaleString()} VND)
            </label>
          ))}
        </div>
      )}

      <button type="submit">Tạo Voucher</button>
    </form>
  );
}
```

---

## 7. Tóm tắt

✅ **Đã có sẵn trong code:**
- Service logic xử lý SPECIFIC_PRODUCTS (validate sản phẩm thuộc shop)
- Service logic xử lý SPECIFIC_USERS (lưu vào voucher_user)
- Service logic xử lý TOP_BUYERS (tự động chọn khách hàng chi tiêu nhiều nhất)

✅ **Mới thêm:**
- API `/api/seller/vouchers/customers` để lấy danh sách khách hàng
- Query methods trong `OrderRepository`:
  - `findDistinctCustomersByShop(Shop shop)`
  - `countByShopAndUser(Shop shop, User user)`
  - `sumTotalByShopAndUser(Shop shop, User user)`

🔧 **Cần làm tiếp:**
- Tạo giao diện Frontend để chọn sản phẩm/khách hàng
- Thêm dropdown hoặc checkbox list để chọn
- Hiển thị thông tin khách hàng (số đơn, tổng chi tiêu)

---

## 8. Lưu ý quan trọng

⚠️ **Validation:**
- Backend đã tự động validate sản phẩm có thuộc shop hay không
- Nếu sản phẩm không thuộc shop, API trả về lỗi ngay lập tức

⚠️ **Chỉ đếm đơn COMPLETED:**
- `orderCount` và `totalSpent` chỉ tính các đơn hàng có status = COMPLETED
- Đơn PENDING, CANCELLED không được tính

⚠️ **Phân quyền:**
- Chỉ SELLER mới có quyền tạo voucher
- Chỉ SELLER mới xem được danh sách khách hàng của shop mình
