# MoMo Web Payment Implementation Guide

## Overview

Hệ thống đã được chuyển sang sử dụng **Web Payment** thay vì QR Code.

Khi khách hàng thanh toán, họ sẽ được redirect sang trang `pay.momo.vn`, nhập OTP, và xác nhận thanh toán.

## Backend API Endpoints

### 1. Tạo thanh toán Web Payment

**POST** `/api/payment/momo/create`

#### Request Body:

```json
{
  "orderId": 123,
  "orderInfo": "Thanh toán đơn hàng #123"
}
```

#### Response (Success):

```json
{
  "success": true,
  "data": {
    "payUrl": "https://pay.momo.vn/web/...",
    "orderId": "ORD-123-UUID",
    "requestId": "REQ-...",
    "amount": 100000,
    "message": "Web Payment URL created successfully. Please redirect user to payUrl to complete payment."
  },
  "message": "Web Payment URL created successfully. Please redirect user to payUrl to complete payment."
}
```

**Important**: Response chỉ chứa `payUrl` - bỏ đi `deeplink` và `qrCodeUrl`.

### 2. Xác nhận thanh toán

Khi user hoàn thành thanh toán trên MoMo, hệ thống sẽ:

1. Nhận callback từ MoMo (IPN) → `POST /api/payment/momo/callback`
2. Redirect user về frontend → `GET /api/payment/momo/return?orderId=...&resultCode=...`

### 3. Kiểm tra trạng thái thanh toán

**GET** `/api/payment/momo/status/{orderId}`

Response:

```json
{
  "success": true,
  "data": {
    "orderId": 123,
    "hasSuccessfulPayment": true,
    "paymentStatus": "SUCCESS",
    "transId": "12345678",
    "amount": 100000
  }
}
```

## Frontend Implementation

### Flow Thanh Toán Web Payment

```javascript
// 1. User click "Thanh toán bằng MoMo" button
async function handleMoMoPayment(orderId) {
  try {
    // Call backend to create web payment
    const response = await fetch("/api/payment/momo/create", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ orderId }),
    });

    const result = await response.json();

    if (result.success && result.data.payUrl) {
      // 2. Redirect user to MoMo payment page
      window.location.href = result.data.payUrl;

      // After payment on MoMo, user will be redirected back to:
      // http://localhost:8081/api/payment/momo/return?orderId=...&resultCode=...
      // which will redirect to: http://localhost:3000/payment/success or /payment/failed
    } else {
      console.error("Failed to create payment:", result.message);
    }
  } catch (error) {
    console.error("Error:", error);
  }
}

// 3. Handle return from MoMo (on success/failed page)
function handlePaymentReturn() {
  const params = new URLSearchParams(window.location.search);
  const orderId = params.get("orderId");
  const resultCode = params.get("resultCode");
  const message = params.get("message");

  if (resultCode === "0") {
    // Success - Payment was successful
    console.log("Payment successful!");
    // Update UI, show success message, redirect to orders page, etc.
  } else {
    // Failed - Payment failed
    console.log("Payment failed:", message);
    // Show error message, allow user to retry
  }
}

// 4. (Optional) Check payment status
async function checkPaymentStatus(orderId) {
  const response = await fetch(`/api/payment/momo/status/${orderId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });

  const result = await response.json();

  if (result.data.hasSuccessfulPayment) {
    console.log("Payment has been confirmed!");
  } else {
    console.log("Payment pending or failed");
  }
}
```

### React Example Component

```jsx
import React from "react";

export function MoMoPaymentButton({ orderId }) {
  const handlePayment = async () => {
    try {
      const response = await fetch("/api/payment/momo/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ orderId }),
      });

      const result = await response.json();

      if (result.success && result.data.payUrl) {
        // Redirect to MoMo payment page
        window.location.href = result.data.payUrl;
      } else {
        alert("Failed to create payment: " + result.message);
      }
    } catch (error) {
      alert("Error: " + error.message);
    }
  };

  return (
    <button onClick={handlePayment} className="btn btn-momo">
      Thanh toán bằng MoMo
    </button>
  );
}
```

## Configuration

### application.properties

```properties
# MoMo Web Payment Config
momo.partnerCode=MOMOBKUN20180529
momo.accessKey=klm05TvNBzhg7h7j
momo.secretKey=at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa
momo.endpoint=https://test-payment.momo.vn/v2/gateway/api/create
momo.redirectUrl=http://localhost:8081/api/payment/momo/return
momo.ipnUrl=http://localhost:8081/api/payment/momo/callback
momo.requestType=payWithMethod  # ← Web Payment request type
momo.paymentTimeout=15
```

**Key Config:**

- `momo.requestType=payWithMethod` - Sử dụng Web Payment
- `momo.redirectUrl` - User được redirect đến đây sau thanh toán
- `momo.ipnUrl` - MoMo gửi callback đến đây

## Payment Flow Diagram

```
┌─────────────┐
│   Frontend  │
└──────┬──────┘
       │ 1. POST /api/payment/momo/create
       ↓
┌─────────────┐          ┌──────────────┐
│   Backend   │────────→ │ MoMo API     │
└──────┬──────┘          └──────────────┘
       │ 2. Return payUrl
       ↓
┌─────────────┐
│   Frontend  │
│ Redirect    │ 3. window.location.href = payUrl
│ to payUrl   │
└──────┬──────┘
       │
       ↓
┌──────────────┐
│ MoMo Payment │ 4. User enters OTP and confirms
│   Page       │
└──────┬───────┘
       │
       ├─ 5a. Payment Success
       │      └→ Callback to /api/payment/momo/callback
       │      └→ Redirect to /api/payment/momo/return
       │      └→ Frontend /payment/success
       │
       └─ 5b. Payment Failed
              └→ Callback to /api/payment/momo/callback
              └→ Redirect to /api/payment/momo/return
              └→ Frontend /payment/failed
```

## Testing

### Test Credentials (Provided by MoMo)

- Partner Code: `MOMOBKUN20180529`
- Access Key: `klm05TvNBzhg7h7j`
- Secret Key: `at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa`
- Endpoint: `https://test-payment.momo.vn/v2/gateway/api/create` (Test environment)

### Test Cases

1. **Successful Payment:**

   - Order ID: 123
   - Amount: 100000 VND
   - Expected: payUrl returned, user redirected to MoMo, payment success, callback received, order status updated

2. **Failed Payment:**

   - User declines on MoMo page
   - Expected: Failed callback, order status remains unpaid

3. **Payment Status Check:**
   - After payment, check `/api/payment/momo/status/{orderId}`
   - Expected: `hasSuccessfulPayment: true`

## Debugging

### Check Logs

```bash
# View backend logs
tail -f /var/log/app.log | grep "MoMo\|Web Payment"

# Search for specific order
tail -f /var/log/app.log | grep "orderId: 123"
```

### Database Query

```sql
-- Check payment transaction
SELECT * FROM payment_transaction WHERE order_id = 123;

-- Check order payment status
SELECT id, total_amount, method, paid_at FROM orders WHERE id = 123;
```

## Troubleshooting

### Issue: payUrl is null

**Solution:** Check if MoMo API endpoint and credentials are correct in application.properties

### Issue: Signature verification failed

**Solution:** Ensure `momo.secretKey` in application.properties matches MoMo account settings

### Issue: Callback not received

**Solution:**

- Check firewall/network settings
- Ensure `momo.ipnUrl` is publicly accessible
- Use ngrok to expose local environment for testing

### Issue: User not redirected after payment

**Solution:**

- Check `momo.redirectUrl` in application.properties
- Ensure frontend can handle redirect URL properly

## Key Changes from QR Code to Web Payment

| Feature        | QR Code                 | Web Payment           |
| -------------- | ----------------------- | --------------------- |
| Endpoint       | MoMo QR generation      | MoMo payment page     |
| Response       | `qrCodeUrl`, `deeplink` | `payUrl` only         |
| User Action    | Scan QR in MoMo app     | Click link, enter OTP |
| Platform       | Mobile-only             | Web + Mobile          |
| Callback       | IPN (same)              | IPN (same)            |
| Implementation | Simple                  | More user-friendly    |

## API Response Comparison

### Before (QR Code):

```json
{
  "payUrl": "...",
  "deeplink": "...",
  "qrCodeUrl": "...",
  "orderId": "...",
  "requestId": "..."
}
```

### After (Web Payment):

```json
{
  "payUrl": "https://pay.momo.vn/web/...",
  "orderId": "...",
  "requestId": "...",
  "amount": 100000,
  "message": "Web Payment URL created successfully..."
}
```
