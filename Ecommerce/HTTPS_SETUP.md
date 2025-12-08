# Hướng dẫn thiết lập HTTPS cho Spring Boot

## ⚡ Cách nhanh: Sử dụng Script có sẵn

```powershell
cd D:\Proj_Nam4\PBL6_E-Commerce\Ecommerce
powershell -ExecutionPolicy Bypass -File .\create-ssl-certificate.ps1
```

Script sẽ tự động tạo `keystore.p12` trong thư mục `src/main/resources`.

---

## 📋 Cách thủ công (nếu cần)

### Bước 1: Tạo Self-Signed Certificate

Mở PowerShell và chạy lệnh sau để tạo certificate:

```powershell
cd D:\Proj_Nam4\PBL6_E-Commerce\Ecommerce\src\main\resources

keytool -genkeypair -alias ecommerce -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365 -storepass password -dname "CN=localhost, OU=Development, O=PBL6 E-Commerce, L=DaNang, ST=DaNang, C=VN" -ext "SAN=dns:localhost,ip:127.0.0.1"
```

### Giải thích các tham số:
- `-alias ecommerce`: Tên alias cho certificate
- `-keyalg RSA`: Thuật toán RSA
- `-keysize 2048`: Độ dài key 2048 bit
- `-storetype PKCS12`: Định dạng keystore
- `-keystore keystore.p12`: Tên file keystore
- `-validity 3650`: Hiệu lực 10 năm (3650 ngày)
- `-storepass password`: Mật khẩu của keystore (đã cấu hình trong application.properties)
- `-dname "..."`: Thông tin Distinguished Name

## Bước 2: Xác nhận file đã được tạo

Sau khi chạy lệnh trên, file `keystore.p12` sẽ được tạo trong thư mục:
```
D:\PBL6\PBL6_E-Commerce\Ecommerce\src\main\resources\keystore.p12
```

## Bước 3: Chạy lại Spring Boot

Backend sẽ tự động chạy trên HTTPS với cấu hình trong `application.properties`:

```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=password
server.ssl.key-alias=ecommerce
```

Backend sẽ chạy trên: **https://localhost:8081**

## Bước 4: Tin cậy certificate trong trình duyệt

Khi truy cập lần đầu, trình duyệt sẽ cảnh báo về certificate không tin cậy (vì đây là self-signed certificate). 

Để bypass:
1. Chrome/Edge: Click "Advanced" → "Proceed to localhost (unsafe)"
2. Firefox: Click "Advanced" → "Accept the Risk and Continue"

## Bước 5: Kiểm tra WebSocket

Frontend (https://localhost:3000) sẽ kết nối đến:
- WebSocket URL: `wss://localhost:8081/ws` (secure WebSocket)
- REST API: `https://localhost:8081/api/...`

Code trong `useNotifications.js` đã được cấu hình để tự động detect protocol:
```javascript
const WS_URL = window.location.protocol === 'https:' 
  ? 'https://localhost:8081/ws' 
  : 'http://localhost:8081/ws';
```

## Lưu ý

- File `keystore.p12` nên được thêm vào `.gitignore` để không commit lên repository
- Trong production, sử dụng certificate hợp lệ từ Let's Encrypt hoặc CA khác
- Password hiện tại là "password" - đổi thành password mạnh hơn trong production
