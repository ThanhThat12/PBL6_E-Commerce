# 📮 **PAYMENT API - POSTMAN COLLECTION**

## 🚀 **IMPORT COLLECTION**

### **Bước 1: Open Postman**
- Click "Collections" tab (left sidebar)
- Click "Import" button
- Select file: `Payment_API.postman_collection.json`

### **Bước 2: Import Environment**
- Click "Environments" tab (left sidebar)
- Click "Import" button
- Select file: `Payment_API_Local.postman_environment.json`

### **Bước 3: Select Environment**
- Click environment dropdown (top right)
- Select "Payment API - Local"

---

## 📋 **COLLECTION STRUCTURE**

### **1. Authentication**
```
├── Login - Buyer1       (POST /api/auth/login)
└── Login - Seller1      (POST /api/auth/login)
```
**Purpose:** Get JWT token để sử dụng cho các requests sau
**Auto-saves token** vào environment variable

### **2. Payment - Create**
```
├── Create MoMo Web Payment - Valid          (POST /api/payment/momo/create)
├── Create MoMo Web Payment - Invalid Order  (POST /api/payment/momo/create)
└── Create MoMo Web Payment - No Token       (POST /api/payment/momo/create)
```
**Purpose:** Tạo thanh toán MoMo Web Payment
**Tests:** Validation errors và security

### **3. Payment - Status**
```
├── Check Payment Status - Valid Order     (GET /api/payment/momo/status/{orderId})
├── Check Payment Status - Invalid Order   (GET /api/payment/momo/status/{orderId})
└── Check Payment Status - No Token        (GET /api/payment/momo/status/{orderId})
```
**Purpose:** Kiểm tra trạng thái thanh toán
**Tests:** Status validation

### **4. Payment - Callback**
```
├── MoMo Callback - Payment Success   (POST /api/payment/momo/callback)
└── MoMo Callback - Payment Failed    (POST /api/payment/momo/callback)
```
**Purpose:** Test callback từ MoMo (IPN)
**Tests:** Callback processing

### **5. Payment - Return**
```
├── MoMo Return - Success   (GET /api/payment/momo/return)
└── MoMo Return - Failed    (GET /api/payment/momo/return)
```
**Purpose:** Test return URL sau thanh toán
**Tests:** Redirect handling

---

## 🎯 **QUICK START WORKFLOW**

### **Workflow 1: Test Full Payment Flow**

```
1. Login - Buyer1
   ✓ Lấy token và username

2. Create MoMo Web Payment - Valid
   ✓ Tạo thanh toán, nhận payUrl

3. Check Payment Status - Valid Order
   ✓ Kiểm tra trạng thái trước thanh toán

4. MoMo Callback - Payment Success
   ✓ Simulate callback từ MoMo

5. Check Payment Status - Valid Order (lại)
   ✓ Kiểm tra trạng thái sau thanh toán

6. MoMo Return - Success
   ✓ Test return URL
```

### **Workflow 2: Test Error Cases**

```
1. Login - Buyer1

2. Create MoMo Web Payment - Invalid Order
   ✓ Kiểm tra error: order không tồn tại

3. Create MoMo Web Payment - No Token
   ✓ Kiểm tra error: 401 Unauthorized

4. Check Payment Status - Invalid Order
   ✓ Kiểm tra error: order không tồn tại

5. MoMo Callback - Payment Failed
   ✓ Test callback thất bại
```

---

## 📝 **VARIABLE SETUP**

### **Environment Variables:**
| Variable | Value | Purpose |
|----------|-------|---------|
| `base_url` | `http://localhost:8080` | Base URL của API server |
| `token` | (auto-set) | JWT token từ login |
| `username` | (auto-set) | Username từ login |
| `orderId` | `1` | Order ID để test |
| `partnerCode` | `MOMOBKUN20180529` | MoMo Partner Code |
| `accessKey` | `klm05TvNBzhg7h7j` | MoMo Access Key |
| `secretKey` | `at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa` | MoMo Secret Key |

**Auto-setup:** Khi bạn chạy Login request, token và username sẽ tự động lưu vào environment.

---

## 🔄 **RUNNING REQUESTS**

### **Individual Request:**
1. Chọn request bất kỳ
2. Click "Send" button
3. Xem response ở tab "Response"

### **Run Entire Folder:**
1. Right-click folder (e.g., "Payment - Create")
2. Click "Run"
3. Xem kết quả cho tất cả requests trong folder

### **Run Collection:**
1. Click "▶ Run" button (ở collection name)
2. Chọn environment "Payment API - Local"
3. Click "Run Payment API" button
4. Xem console output

---

## ✅ **TEST CASES INCLUDED**

Mỗi request có **test scripts** để:
- ✅ Kiểm tra HTTP status code
- ✅ Validate response structure
- ✅ Check error messages
- ✅ Verify field values

**View test results:**
1. Run request
2. Click "Tests" tab ở response panel
3. Xem passing/failing tests

---

## 💳 **PAYMENT FLOW TESTING**

### **1. Create Payment:**
```json
POST /api/payment/momo/create
{
  "orderId": 1,
  "orderInfo": "Thanh toán đơn hàng #1"
}
```
**Expected Response:**
```json
{
  "success": true,
  "data": {
    "payUrl": "https://pay.momo.vn/web/...",
    "orderId": "ORD-1-UUID",
    "requestId": "REQ-...",
    "amount": 100000,
    "message": "Web Payment URL created successfully..."
  }
}
```

### **2. Simulate Callback:**
```json
POST /api/payment/momo/callback
{
  "partnerCode": "MOMOBKUN20180529",
  "orderId": "ORD-1-UUID",
  "resultCode": 0,
  "message": "Successful",
  "signature": "signature_here"
}
```

### **3. Check Status:**
```json
GET /api/payment/momo/status/1
```
**Expected Response:**
```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "hasSuccessfulPayment": true,
    "paymentStatus": "SUCCESS",
    "transId": "12345678",
    "amount": 100000
  }
}
```

---

## 🔐 **AUTHENTICATION NOTE**

Tất cả `/api/payment/momo/create` và `/api/payment/momo/status` endpoints yêu cầu **JWT token** trong header:
```
Authorization: Bearer {token}
```

**Auto-setup:** Chỉ cần chạy "Login" request trước, token sẽ tự động thêm vào headers.

**Exception:** `/api/payment/momo/callback` và `/api/payment/momo/return` không cần authentication (public endpoints).

---

## 🎯 **TESTING CHECKLIST**

- [ ] Import collection thành công
- [ ] Import environment thành công
- [ ] Login - Buyer1 thành công (token được set)
- [ ] Create payment trả về 200 + payUrl
- [ ] Check status trả về payment info
- [ ] Callback success được xử lý
- [ ] Return URL hoạt động
- [ ] Error cases fail đúng cách
- [ ] Security tests return 401
- [ ] All tests pass khi run collection

---

## 🚀 **NEXT STEPS**

1. **Integrate với Order APIs:**
   - Import Order API collection
   - Test create order → payment → status check

2. **Real Payment Testing:**
   - Click payUrl từ create response
   - Complete payment trên MoMo test environment
   - Verify callback và status update

3. **Frontend Integration:**
   - Implement payment button trong React
   - Handle redirect sau thanh toán
   - Show payment status

---

## 📞 **TROUBLESHOOTING**

### **Issue: 401 Unauthorized**
**Solution:**
- Chạy "Login" request trước
- Kiểm tra token đã được set: `{{token}}`
- Nếu vẫn error, login lại

### **Issue: 400 Bad Request (create payment)**
**Solution:**
- Kiểm tra orderId tồn tại trong database
- Kiểm tra order chưa được thanh toán
- Kiểm tra JWT token hợp lệ

### **Issue: Callback not processed**
**Solution:**
- Kiểm tra signature trong callback body
- Verify partnerCode, orderId format
- Check resultCode values (0 = success, others = failed)

### **Issue: Status shows unpaid after callback**
**Solution:**
- Kiểm tra callback được gọi trước status check
- Verify orderId mapping đúng
- Check database payment_transaction table

---

## 🔧 **MOMO CONFIGURATION**

### **Test Environment:**
- Endpoint: `https://test-payment.momo.vn/v2/gateway/api/create`
- Partner Code: `MOMOBKUN20180529`
- Access Key: `klm05TvNBzhg7h7j`
- Secret Key: `at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa`

### **Production Environment:**
- Endpoint: `https://payment.momo.vn/v2/gateway/api/create`
- Sử dụng credentials thật từ MoMo

---

**Payment API Postman Collection - Ready to test MoMo Web Payment!** 🎉
