# GHN Webhook Integration Guide

## 📡 Webhook Endpoint

GHN sẽ gửi POST request đến endpoint sau khi có thay đổi trạng thái đơn hàng:

```
POST https://your-domain.com/api/webhook/ghn/status
```

## 🔧 Cấu hình Webhook trên GHN

1. Đăng nhập vào GHN Dashboard: https://khachhang.giaohangnhanh.vn
2. Vào **Cài đặt** → **Webhook**
3. Nhập URL webhook: `https://your-domain.com/api/webhook/ghn/status`
4. Chọn các sự kiện muốn nhận:
   - ✅ Đã lấy hàng (picked)
   - ✅ Đang giao (delivering)
   - ✅ Đã giao (delivered)
   - ✅ Giao thất bại (delivery_fail)
   - ✅ Hoàn trả (return/returned)
   - ✅ Hủy đơn (cancel)

## 📥 Payload GHN gửi đến

```json
{
  "OrderCode": "GHN123456",
  "Status": "delivered",
  "StatusName": "Đã giao hàng",
  "Time": "2025-11-28 18:30:00",
  "Description": "Giao hàng thành công",
  "Reason": "",
  "ReasonCode": ""
}
```

## 🔄 Mapping trạng thái GHN → Order Status

| GHN Status          | Order Status | Mô tả                        |
| ------------------- | ------------ | ---------------------------- |
| `ready_to_pick`     | `SHIPPING`   | Chờ lấy hàng                 |
| `picking`           | `SHIPPING`   | Đang lấy hàng                |
| `picked`            | `SHIPPING`   | Đã lấy hàng                  |
| `storing`           | `SHIPPING`   | Đang lưu kho                 |
| `transporting`      | `SHIPPING`   | Đang vận chuyển              |
| `sorting`           | `SHIPPING`   | Đang phân loại               |
| `delivering`        | `SHIPPING`   | Đang giao hàng               |
| `delivered`         | `COMPLETED`  | ✅ Đã giao thành công        |
| `delivery_fail`     | `SHIPPING`   | Giao thất bại (chờ giao lại) |
| `waiting_to_return` | `SHIPPING`   | Chờ hoàn trả                 |
| `return`            | `SHIPPING`   | Đang hoàn trả                |
| `returned`          | `SHIPPING`   | Đã hoàn về shop              |
| `cancel`            | `CANCELLED`  | ❌ Đơn bị hủy                |
| `exception`         | `SHIPPING`   | Bất thường (cần xử lý)       |
| `damage`            | `SHIPPING`   | Hàng hư hỏng                 |
| `lost`              | `SHIPPING`   | Thất lạc                     |

## 📱 WebSocket Notifications cho Buyer

Khi có cập nhật từ GHN webhook, hệ thống tự động gửi notification đến buyer qua WebSocket:

**Channel**: `/topic/orderws/{buyerId}`

**Payload mẫu**:

```json
{
  "type": "ORDER_STATUS_UPDATE",
  "orderId": 123,
  "orderStatus": "COMPLETED",
  "ghnStatus": "delivered",
  "ghnStatusName": "Đã giao hàng",
  "message": "🎉 Đơn hàng #123 đã được giao thành công!",
  "timestamp": 1732795800000
}
```

## 🎯 Các thông báo tự động

| GHN Status        | Icon | Message                                 |
| ----------------- | ---- | --------------------------------------- |
| `delivered`       | 🎉   | Đơn hàng #{id} đã được giao thành công! |
| `delivering`      | 🚚   | Đơn hàng #{id} đang được giao đến bạn   |
| `picked`          | 📦   | Đơn hàng #{id} đã được lấy hàng         |
| `return/returned` | 🔄   | Đơn hàng #{id} đang được hoàn trả       |
| `delivery_fail`   | ⚠️   | Giao hàng thất bại cho đơn #{id}        |

## 🧪 Test Webhook

### Sử dụng ngrok (development)

1. Chạy backend: `mvn spring-boot:run`
2. Chạy ngrok: `ngrok http 8081`
3. Copy HTTPS URL (vd: `https://abc123.ngrok.io`)
4. Cập nhật webhook URL trên GHN: `https://abc123.ngrok.io/api/webhook/ghn/status`

### Test với curl

```bash
curl -X POST https://your-domain.com/api/webhook/ghn/status \
  -H "Content-Type: application/json" \
  -d '{
    "OrderCode": "GHN123456",
    "Status": "delivered",
    "StatusName": "Đã giao hàng",
    "Time": "2025-11-28 18:30:00"
  }'
```

## 📊 Log & Monitoring

Backend sẽ log tất cả webhook events:

```
✅ Updated shipment 45 status: picking → delivered
✅ Updated order 123 status: SHIPPING → COMPLETED
✅ Sent notification to buyer 5 for order 123: 🎉 Đơn hàng #123 đã được giao thành công!
```

## ⚠️ Lưu ý

1. **Webhook phải public**: Endpoint phải accessible từ internet
2. **HTTPS required**: GHN chỉ gửi đến HTTPS endpoints (production)
3. **Response nhanh**: Webhook nên response trong < 5s
4. **Idempotent**: Có thể nhận duplicate webhooks, cần handle
5. **Retry**: GHN sẽ retry nếu webhook fail (tối đa 3 lần)

## 🔐 Security (Optional)

Nếu muốn verify webhook từ GHN (tránh fake requests):

1. GHN cung cấp signature trong header
2. Verify signature với token/secret của shop
3. Reject nếu signature không match

```java
@PostMapping("/status")
public Map<String,Object> updateStatus(
        @RequestBody Map<String,Object> payload,
        @RequestHeader(value = "X-GHN-Signature", required = false) String signature) {

    // Verify signature (nếu GHN cung cấp)
    if (!verifySignature(payload, signature)) {
        return Map.of("success", false, "error", "Invalid signature");
    }

    // Process webhook...
}
```

## 🎯 Flow hoàn chỉnh

```
1. Seller tạo shipment → GHN nhận đơn
2. GHN lấy hàng → Webhook: "picked" → Order: SHIPPING → Notify buyer 📦
3. GHN đang giao → Webhook: "delivering" → Order: SHIPPING → Notify buyer 🚚
4. GHN giao thành công → Webhook: "delivered" → Order: COMPLETED → Notify buyer 🎉
5. Buyer nhận thông báo realtime qua WebSocket
```

## ✅ Implementation Checklist

- [x] Webhook endpoint `/api/webhook/ghn/status`
- [x] Parse GHN payload
- [x] Update shipment status
- [x] Update order status based on GHN status
- [x] Send WebSocket notification to buyer
- [x] Log all webhook events
- [x] Handle errors gracefully
- [ ] Setup webhook URL on GHN Dashboard (production)
- [ ] Test with real GHN orders
- [ ] Monitor webhook logs

## 📚 Tài liệu GHN

- Webhook API: https://api.ghn.vn/home/docs/detail?id=74
- Danh sách status: https://api.ghn.vn/home/docs/detail?id=122
- Dashboard: https://khachhang.giaohangnhanh.vn
