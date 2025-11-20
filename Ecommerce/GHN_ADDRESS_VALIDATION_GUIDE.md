# HƯỚNG DẪN: XÁC ĐỊNH ĐỊA CHỈ HỢP LỆ CHO GHN

## 🎯 Cách GHN hoạt động

GHN có **network logistics** với nhiều hub/trung tâm phân phối. Không phải tất cả địa chỉ đều được support bởi tất cả services.

### Service Types phổ biến:
1. **Express (53320)** - Nhanh, thường support các thành phố lớn
2. **Standard (53321)** - Tiêu chuẩn, coverage rộng hơn
3. **Economy** - Chậm hơn nhưng rẻ và support nhiều vùng xa

## ✅ Địa chỉ HỢP LỆ là gì?

Địa chỉ được coi là **hợp lệ** khi:

### 1. Có đầy đủ thông tin GHN cần:
```javascript
{
  districtId: 1444,      // ✅ Required - GHN District ID
  wardCode: "20308",     // ✅ Required - GHN Ward Code
  province: "Hà Nội",    // Tên tỉnh/thành
  district: "Quận Ba Đình", // Tên quận/huyện
  ward: "Phường Điện Biên",  // Tên phường/xã
  fullAddress: "123 Điện Biên Phủ" // Số nhà, tên đường
}
```

### 2. GHN có hỗ trợ route:
- Từ shop address (district/ward)
- Đến buyer address (district/ward)
- Với ít nhất 1 service

## 🔍 Cách kiểm tra địa chỉ

### Phương pháp 1: Test trực tiếp trên UI

1. **Vào trang Payment**
2. **Chọn địa chỉ** (hoặc nhập mới)
3. **Xem phần "Dịch vụ vận chuyển"**:
   - ✅ Có danh sách services → Địa chỉ HỢP LỆ
   - ❌ "Không có dịch vụ" → Địa chỉ KHÔNG hợp lệ
4. **Chọn từng service** và xem:
   - ✅ Hiển thị phí ship (VD: 25.000đ) → Service HỢP LỆ
   - ⚠️ "Vui lòng chọn dịch vụ khác" → Service này KHÔNG support

### Phương pháp 2: Check trong Database

```sql
-- Lấy tất cả địa chỉ của user
SELECT 
    id,
    type_address,
    province_name,
    district_name,
    ward_name,
    district_id,
    ward_code,
    full_address
FROM addresses
WHERE user_id = 1 -- YOUR_USER_ID
  AND type_address = 'HOME'
ORDER BY primary_address DESC;

-- Địa chỉ HỢP LỆ phải có:
-- ✅ district_id IS NOT NULL
-- ✅ ward_code IS NOT NULL  
-- ✅ district_id != 0
-- ✅ ward_code != '' AND ward_code != '0'
```

### Phương pháp 3: Test API trực tiếp

**Endpoint**: `POST /api/checkout/available-services`

```bash
curl -X POST http://localhost:8081/api/checkout/available-services \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "shopId": 3,
    "addressId": 5,
    "cartItemIds": [1, 2]
  }'
```

**Response nếu HỢP LỆ**:
```json
{
  "code": 200,
  "message": "Lấy danh sách dịch vụ thành công",
  "data": [{
    "services": [
      {
        "service_id": 53320,
        "short_name": "Express",
        "service_type_id": 2
      },
      {
        "service_id": 53321,
        "short_name": "Standard",
        "service_type_id": 2
      }
    ],
    "totalWeight": 200,
    "shopAddress": {...},
    "buyerAddress": {...}
  }]
}
```

**Response nếu KHÔNG hợp lệ**:
```json
{
  "code": 400,
  "message": "Không tìm thấy dịch vụ vận chuyển cho địa chỉ này",
  "type": "ERROR"
}
```

## 📍 Địa chỉ phổ biến HỢP LỆ (đã test)

### Hà Nội (Thường OK với hầu hết services)
```
- Quận Ba Đình (District: 1444)
- Quận Hoàn Kiếm (District: 1442)
- Quận Đống Đa (District: 1451)
- Quận Cầu Giấy (District: 1452)
- Quận Hai Bà Trưng (District: 1454)
```

### TP.HCM (Thường OK)
```
- Quận 1 (District: 1460)
- Quận 3 (District: 1462)
- Quận 5 (District: 1458)
- Quận 10 (District: 1463)
- Quận Tân Bình (District: 1455)
```

## ⚠️ Địa chỉ thường GẶP VẤN ĐỀ

### 1. Vùng xa, miền núi
- Một số huyện miền núi
- Hải đảo
- → Chỉ support Economy service, không support Express

### 2. Địa chỉ thiếu thông tin
```javascript
// ❌ BAD - Thiếu ward_code
{
  districtId: 1444,
  wardCode: null  // ← Thiếu
}

// ✅ GOOD
{
  districtId: 1444,
  wardCode: "20308"
}
```

### 3. District/Ward code sai
```javascript
// ❌ BAD - Code không tồn tại trong GHN
{
  districtId: 99999,  // ← Code này không có trong GHN
  wardCode: "00000"
}
```

## 🛠️ Tool: Validate Address Script

Tạo file `validate-address.sql`:

```sql
-- ===================================
-- VALIDATE ADDRESS FOR GHN
-- ===================================

-- Check xem địa chỉ có đủ thông tin GHN không
SELECT 
    id,
    CASE 
        WHEN district_id IS NULL THEN '❌ Missing district_id'
        WHEN ward_code IS NULL THEN '❌ Missing ward_code'
        WHEN district_id = 0 THEN '❌ Invalid district_id (0)'
        WHEN ward_code = '' THEN '❌ Invalid ward_code (empty)'
        ELSE '✅ Valid GHN data'
    END as validation_status,
    type_address,
    province_name,
    district_name,
    ward_name,
    district_id,
    ward_code,
    full_address,
    user_id
FROM addresses
WHERE user_id = 1  -- YOUR_USER_ID
ORDER BY 
    CASE 
        WHEN district_id IS NOT NULL AND ward_code IS NOT NULL 
            AND district_id != 0 AND ward_code != '' 
        THEN 0 
        ELSE 1 
    END,
    primary_address DESC;

-- Count valid vs invalid addresses
SELECT 
    COUNT(*) as total,
    SUM(CASE 
        WHEN district_id IS NOT NULL AND ward_code IS NOT NULL 
            AND district_id != 0 AND ward_code != '' 
        THEN 1 ELSE 0 
    END) as valid_for_ghn,
    SUM(CASE 
        WHEN district_id IS NULL OR ward_code IS NULL 
            OR district_id = 0 OR ward_code = '' 
        THEN 1 ELSE 0 
    END) as invalid_for_ghn
FROM addresses
WHERE user_id = 1;  -- YOUR_USER_ID
```

## 💡 Tips để có địa chỉ HỢP LỆ

### 1. Khi user ADD ADDRESS mới:
- ✅ Force chọn Tỉnh/Thành phố từ GHN API
- ✅ Force chọn Quận/Huyện từ GHN API  
- ✅ Force chọn Phường/Xã từ GHN API
- ❌ KHÔNG cho phép nhập free text cho Province/District/Ward

### 2. Validate ngay khi save:
```javascript
// Frontend validation
const validateAddress = (address) => {
  if (!address.districtId || address.districtId === 0) {
    return "Vui lòng chọn Quận/Huyện";
  }
  if (!address.wardCode || address.wardCode === '') {
    return "Vui lòng chọn Phường/Xã";
  }
  return null; // Valid
};
```

### 3. Test ngay sau khi add:
```javascript
// Sau khi user save address, test luôn xem có services không
const testAddress = async (addressId) => {
  const services = await api.post('/checkout/available-services', {
    shopId: 1, // Test shop
    addressId: addressId,
    cartItemIds: []
  });
  
  if (services.data?.length === 0) {
    toast.warning("⚠️ Địa chỉ này có thể không được GHN hỗ trợ đầy đủ");
  }
};
```

## 📊 Bảng tra cứu nhanh

| Tình huống | Nguyên nhân | Giải pháp |
|------------|-------------|-----------|
| Không có services nào | Địa chỉ vùng xa/không support | Chọn địa chỉ khác hoặc COD |
| Service A OK, Service B fail | Route không support service B | Dùng Service A |
| Tất cả services đều fail route | Combination district kỳ lạ | Check district_id/ward_code đúng chưa |
| API lỗi 400 - "Địa chỉ không tồn tại" | addressId sai hoặc không thuộc user | Check addressId trong DB |
| API lỗi 400 - "Không tìm thấy STORE" | Shop chưa có address STORE | Seller cần add STORE address |

## 🎯 Recommended Flow

### For Buyers:
1. **Thêm địa chỉ mới** → Chọn từ GHN dropdown
2. **Validate** → Hệ thống check GHN services
3. **Show indicator**:
   - ✅ "Địa chỉ hợp lệ - 2 dịch vụ có sẵn"
   - ⚠️ "Địa chỉ hạn chế - Chỉ 1 dịch vụ"
   - ❌ "Địa chỉ không được hỗ trợ"

### For Sellers:
1. **Bắt buộc có STORE address**
2. **Validate** STORE address có support shipping không
3. **Warning** nếu STORE ở vùng khó ship

## ✅ Quick Test Checklist

Để test xem địa chỉ có OK không:

- [ ] District ID tồn tại (không null, không 0)
- [ ] Ward Code tồn tại (không null, không blank)
- [ ] Call API available-services → trả về ít nhất 1 service
- [ ] Chọn service → calculate-fee → trả về fee > 0
- [ ] Đặt hàng test → Order được tạo thành công

## 🔗 GHN Documentation

- GHN API Docs: https://api.ghn.vn/home/docs/detail
- Master Data: https://api.ghn.vn/home/docs/detail?id=80
- Calculate Fee: https://api.ghn.vn/home/docs/detail?id=76

---

**TL;DR**: Địa chỉ HỢP LỆ = Có district_id + ward_code + GHN available-services API trả về ít nhất 1 service!
