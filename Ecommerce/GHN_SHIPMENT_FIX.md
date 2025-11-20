# GHN Shipment Creation Fix

## Vấn đề

GHN API trả về lỗi: "GHN không tạo được vận đơn. Vui lòng kiểm tra địa chỉ giao hàng."

## Nguyên nhân chính

1. **Thiếu thông tin bắt buộc** trong request GHN:

   - `from_district_id`: Mã quận/huyện kho hàng (bắt buộc)
   - `from_ward_code`: Mã phường/xã kho hàng (bắt buộc)
   - `shop_id`: ID shop trên GHN (bắt buộc)

2. **Địa chỉ giao hàng không hợp lệ**:

   - `to_district_id` phải là số nguyên (integer)
   - `to_ward_code` phải là mã hợp lệ của GHN
   - Địa chỉ chi tiết (`to_address`) không được để trống

3. **Thiếu các trường mặc định** của GHN API

## Đã sửa

### 1. **OrderService.java** - Cải thiện validation và logging

- ✅ Thêm validation cho tất cả trường bắt buộc trước khi tạo order
- ✅ Parse `to_district_id` thành integer với error handling
- ✅ Thêm default dimensions cho package (15x15x15 cm)
- ✅ Thêm default weight nếu không có (200g)
- ✅ Log chi tiết payload GHN trước khi gửi
- ✅ Cải thiện error message với hướng dẫn cụ thể

### 2. **GhnService.java** - Cải thiện error handling và validation

- ✅ Validate `shop_id` bắt buộc phải có
- ✅ Validate `from_district_id` và `from_ward_code` bắt buộc
- ✅ Parse `shop_id` và `from_district_id` thành integer
- ✅ Log chi tiết request/response của GHN API
- ✅ Catch và xử lý lỗi HTTP 4xx/5xx từ GHN
- ✅ Parse error message từ GHN response body

## Cấu hình trong application.properties

```properties
# GHN config
ghn.api.url=https://dev-online-gateway.ghn.vn/shiip/public-api
ghn.token=0cb9d939-afca-11f0-b040-4e257d8388b4
ghn.shop-id=197701
ghn.from-district-id=1
ghn.from-ward-code=20308
```

**⚠️ LƯU Ý QUAN TRỌNG:**

- `ghn.from-district-id` và `ghn.from-ward-code` phải là **mã GHN chính xác** của địa chỉ kho hàng
- Hiện tại đang dùng `from-district-id=1` (có thể không đúng)
- **Cần kiểm tra và cập nhật lại mã chính xác**

## Cách lấy mã GHN chính xác

### 1. Lấy mã tỉnh/thành (Province ID)

```bash
curl -X GET "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/province" \
  -H "Token: 0cb9d939-afca-11f0-b040-4e257d8388b4"
```

### 2. Lấy mã quận/huyện (District ID)

```bash
# Thay {province_id} bằng ProvinceID từ bước 1
curl -X POST "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district" \
  -H "Token: 0cb9d939-afca-11f0-b040-4e257d8388b4" \
  -H "Content-Type: application/json" \
  -d '{"province_id": {province_id}}'
```

### 3. Lấy mã phường/xã (Ward Code)

```bash
# Thay {district_id} bằng DistrictID từ bước 2
curl -X POST "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward" \
  -H "Token: 0cb9d939-afca-11f0-b040-4e257d8388b4" \
  -H "Content-Type: application/json" \
  -d '{"district_id": {district_id}}'
```

### 4. Kiểm tra Shop ID

```bash
curl -X GET "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shop/all" \
  -H "Token: 0cb9d939-afca-11f0-b040-4e257d8388b4"
```

## Cách test

### 1. Khởi động lại backend

```bash
cd D:\PBL6\PBL6_E-Commerce\Ecommerce
mvn clean install
mvn spring-boot:run
```

### 2. Tạo order mới với COD

Frontend cần gửi request với đầy đủ thông tin:

```json
{
  "items": [
    {
      "variantId": 1,
      "quantity": 1
    }
  ],
  "method": "COD",
  "receiverName": "Nguyễn Văn A",
  "receiverPhone": "0901234567",
  "receiverAddress": "123 Đường ABC",
  "toDistrictId": "1442",
  "toWardCode": "21211",
  "province": "Hồ Chí Minh",
  "district": "Quận 1",
  "ward": "Phường Bến Nghé",
  "weightGrams": 500,
  "codAmount": 100000,
  "shippingFee": 30000
}
```

### 3. Kiểm tra logs

Sau khi gửi request, kiểm tra console backend:

✅ **Thành công:**

```
📦 GHN Payload for order #123: to_district_id=1442, to_ward_code=21211, to_address=123 Đường ABC
🚀 Creating GHN shipment with payload: {...}
✅ GHN shipment created successfully: {...}
✅ Shipment created successfully for COD order: 123
```

❌ **Thất bại:**

```
❌ GHN API Error Response: {"code":400,"message":"District không tồn tại"}
❌ GHN shipment creation failed for COD order: GHN API error...
```

## Các lỗi thường gặp và cách fix

### Lỗi 1: "District không tồn tại"

**Nguyên nhân:** `from_district_id` hoặc `to_district_id` không đúng

**Fix:**

- Kiểm tra lại mã district bằng API GHN master-data
- Đảm bảo `to_district_id` là **integer** không phải string
- Cập nhật `ghn.from-district-id` trong application.properties

### Lỗi 2: "Ward không tồn tại"

**Nguyên nhân:** `from_ward_code` hoặc `to_ward_code` không đúng

**Fix:**

- Ward code phải là **string** (VD: "20308", không phải 20308)
- Lấy ward code chính xác từ API GHN
- Cập nhật `ghn.from-ward-code` trong application.properties

### Lỗi 3: "Shop không tồn tại"

**Nguyên nhân:** `shop_id` không đúng hoặc token không có quyền

**Fix:**

- Gọi API `/v2/shop/all` để lấy danh sách shop
- Cập nhật `ghn.shop-id` trong application.properties
- Kiểm tra token có quyền truy cập shop

### Lỗi 4: "COD amount không hợp lệ"

**Nguyên nhân:**

- `payment_type_id` không khớp với `cod_amount`
- Service không hỗ trợ COD

**Fix:**

- Nếu COD > 0 → `payment_type_id = 2`
- Nếu COD = 0 → `payment_type_id = 1`
- Code đã tự động xử lý trong GhnService

## Frontend cần check

### 1. Đảm bảo gửi đúng format

- `toDistrictId`: String có thể parse thành integer (VD: "1442")
- `toWardCode`: String (VD: "21211")
- `method`: "COD", "MOMO", hoặc "BANK_TRANSFER"

### 2. Lấy mã GHN từ dropdown

Frontend nên:

1. Gọi API GHN để lấy danh sách tỉnh/quận/phường
2. Hiển thị dropdown cho user chọn
3. Lưu **mã GHN** (không phải tên) vào form
4. Gửi cả mã và tên lên backend

### 3. Validate trước khi submit

```javascript
if (!formData.receiverName)
  throw new Error("Tên người nhận không được để trống");
if (!formData.receiverPhone)
  throw new Error("Số điện thoại không được để trống");
if (!formData.receiverAddress) throw new Error("Địa chỉ không được để trống");
if (!formData.toDistrictId) throw new Error("Chưa chọn quận/huyện");
if (!formData.toWardCode) throw new Error("Chưa chọn phường/xã");
```

## Checklist trước khi test

- [ ] Cấu hình `ghn.shop-id` chính xác
- [ ] Cấu hình `ghn.from-district-id` chính xác (mã quận kho hàng)
- [ ] Cấu hình `ghn.from-ward-code` chính xác (mã phường kho hàng)
- [ ] Frontend gửi `toDistrictId` là số (dạng string)
- [ ] Frontend gửi `toWardCode` là mã GHN hợp lệ
- [ ] Địa chỉ chi tiết không để trống
- [ ] Method = "COD" cho đơn COD
- [ ] Restart backend sau khi sửa code

## Kết luận

Sau khi fix:

1. ✅ Validation đầy đủ trước khi tạo order
2. ✅ Error message rõ ràng, hướng dẫn cụ thể
3. ✅ Log chi tiết để debug
4. ✅ Xử lý lỗi HTTP từ GHN
5. ✅ Tự động thêm các field bắt buộc

**Action tiếp theo:**

1. Kiểm tra và cập nhật `ghn.from-district-id` và `ghn.from-ward-code` trong application.properties
2. Restart backend
3. Test tạo order COD
4. Kiểm tra logs để debug nếu còn lỗi
