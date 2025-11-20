# GHN SHIPPING FEE ERROR - ROUTE NOT FOUND

## ❌ Lỗi
```
GHN Error: route not found service
```

## 🔍 Nguyên nhân
GHN không hỗ trợ dịch vụ vận chuyển đã chọn cho tuyến đường cụ thể:
- Từ: District 1542, Ward 21012 (Shop)
- Đến: District 1444, Ward 20308 (Buyer)  
- Service: 53321

## ✅ Đã fix

### Backend (CheckoutController.java)
- ✅ Bắt lỗi "route not found" riêng
- ✅ Trả về message rõ ràng: "GHN không hỗ trợ dịch vụ này..."
- ✅ Return error type: `GHN_ROUTE_NOT_FOUND`

### Frontend (PaymentPage.jsx)
- ✅ Hiển thị toast.warning thay vì toast.error
- ✅ Message: "⚠️ Dịch vụ này không hỗ trợ tuyến đường của bạn. Vui lòng chọn dịch vụ khác"
- ✅ Return shipping fee = 0 để user có thể chọn service khác

## 🎯 Giải pháp cho user

### Cách 1: Chọn dịch vụ khác
1. Trong trang Payment, user sẽ thấy **dropdown list các dịch vụ GHN**
2. Nếu service đầu tiên bị lỗi → Hiện warning
3. User **chọn service khác** từ danh sách
4. Shipping fee sẽ được tính lại tự động

### Cách 2: Đổi địa chỉ
- Nếu tất cả services đều fail
- User có thể chọn địa chỉ giao hàng khác
- Hoặc cập nhật ward/district code

### Cách 3: Sử dụng COD (temporary)
- Nếu không tính được phí ship
- User vẫn có thể đặt hàng COD
- Seller sẽ liên hệ sau để xác nhận phí ship

## 📊 Test flow

### Test Case 1: Happy Path
1. User chọn địa chỉ giao hàng
2. GHN trả về 2-3 services available
3. Service đầu tiên được auto-select
4. Calculate fee thành công
5. Hiển thị shipping fee
6. User thanh toán

### Test Case 2: Route Not Found (Đã fix)
1. User chọn địa chỉ giao hàng
2. GHN trả về services
3. Service đầu tiên auto-select
4. Calculate fee **FAIL** - route not found
5. ✅ Hiển thị warning: "Vui lòng chọn dịch vụ khác"
6. User chọn service thứ 2 từ dropdown
7. Calculate fee thành công cho service 2
8. Hiển thị shipping fee
9. User thanh toán

### Test Case 3: No Available Services
1. User chọn địa chỉ giao hàng ở vùng xa/không hỗ trợ
2. GHN trả về empty services array hoặc error
3. ✅ Hiển thị message: "Không có dịch vụ vận chuyển"
4. Suggest: Đổi địa chỉ hoặc liên hệ shop

## 🔧 Debugging

### Check GHN Services Available
```javascript
// Frontend console
console.log('GHN Services:', ghnServices);
// Should show: { "3": [{ service_id: 53320, ... }, { service_id: 53321, ... }] }
```

### Check Selected Service
```javascript
console.log('Selected Services:', selectedServices);
// Should show: { "3": { service_id: 53320, service_type_id: 2, ... } }
```

### Check Backend Logs
```
[GHN] Extracted services array: [...]
[Service Select] shopId: 3 service: {...}
[Fee Calc] Payload: {...}
```

## 💡 Improvements (Future)

### 1. Auto-fallback to another service
Khi service đầu tiên fail, tự động thử service thứ 2:
```javascript
const handleServiceSelect = async (shopId, service) => {
  const fee = await calculateShippingFeeForService(...);
  
  if (fee === 0 && ghnServices[shopId]?.length > 1) {
    // Try next service automatically
    const nextService = ghnServices[shopId].find(s => s.service_id !== service.service_id);
    if (nextService) {
      await handleServiceSelect(shopId, nextService);
    }
  }
};
```

### 2. Show service compatibility indicator
Hiển thị icon "✅ Available" hoặc "⚠️ May not support" cho mỗi service

### 3. Cache successful routes
Lưu lại các routes đã success để prioritize cho lần sau

### 4. Batch validate services
Khi load services, validate tất cả services cùng lúc và chỉ show những service available

## 📝 Notes

- GHN "route not found" là **normal behavior**, không phải bug
- Một địa chỉ có thể support service A nhưng không support service B
- User cần được hướng dẫn rõ để chọn service khác
- Shipping fee = 0 không phải lỗi fatal, user vẫn có thể đặt hàng COD

## ✅ Checklist

- [x] Backend bắt lỗi route not found
- [x] Frontend hiển thị warning message
- [x] User có thể chọn service khác
- [ ] Test với nhiều địa chỉ khác nhau
- [ ] Test với tất cả payment methods (MoMo, SportyPay, COD)
- [ ] Verify admin wallet sau payment (SportyPay) ✅
