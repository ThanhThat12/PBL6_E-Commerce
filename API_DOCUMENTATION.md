# Sport Commerce - Authentication API Documentation

## Tổng quan

Hệ thống đăng ký người dùng với xác thực OTP qua email cho e-commerce platform Sport Commerce.

## Base URL

```
http://localhost:8080/api/auth
```

## Công nghệ sử dụng

- Spring Boot 3.5.6
- Spring Security với JWT
- MySQL Database
- JavaMailSender (SMTP Email)
- BCrypt Password Encoder

---

## API Endpoints

### 1. Đăng ký - Bước 1: Gửi OTP

Gửi mã OTP đến email người dùng để bắt đầu quá trình đăng ký.

**Endpoint:** `POST /api/auth/register`

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "Password123",
  "username": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "0912345678"
}
```

**Request Fields:**

| Field     | Type   | Required | Validation                              |
| --------- | ------ | -------- | --------------------------------------- |
| email     | String | Yes      | Email hợp lệ                            |
| password  | String | Yes      | Min 8 ký tự, có chữ hoa, chữ thường, số |
| username  | String | No       | 3-100 ký tự                             |
| firstName | String | No       | -                                       |
| lastName  | String | No       | -                                       |
| phone     | String | No       | Format: 0[3\|5\|7\|8\|9]xxxxxxxx        |

**Success Response (200 OK):**

```json
{
  "success": true,
  "message": "Mã OTP đã được gửi đến email của bạn",
  "data": {
    "email": "user@example.com",
    "expiresInMinutes": 5
  }
}
```

**Error Responses:**

**409 Conflict** - Email đã tồn tại:

```json
{
  "success": false,
  "message": "Email đã được đăng ký",
  "data": {
    "message": "Email đã được đăng ký",
    "status": 409,
    "error": "Conflict",
    "path": "/api/auth/register",
    "timestamp": "2024-10-09T10:30:00"
  }
}
```

**400 Bad Request** - Username/Phone đã tồn tại:

```json
{
  "success": false,
  "message": "Username đã tồn tại",
  "data": {
    "message": "Username đã tồn tại",
    "status": 400,
    "error": "Bad Request",
    "path": "/api/auth/register",
    "timestamp": "2024-10-09T10:30:00"
  }
}
```

**400 Bad Request** - Validation Error:

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "message": "Dữ liệu không hợp lệ",
    "status": 400,
    "error": "Bad Request",
    "path": "/api/auth/register",
    "timestamp": "2024-10-09T10:30:00",
    "errors": {
      "email": "Email không hợp lệ",
      "password": "Mật khẩu phải có ít nhất 8 ký tự"
    }
  }
}
```

---

### 2. Xác thực OTP - Bước 2: Hoàn tất đăng ký

Xác thực mã OTP và tạo tài khoản người dùng.

**Endpoint:** `POST /api/auth/verify-otp`

**Request Body:**

```json
{
  "email": "user@example.com",
  "otpCode": "123456",
  "password": "Password123",
  "username": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "0912345678"
}
```

**Request Fields:**

| Field     | Type   | Required | Description                  |
| --------- | ------ | -------- | ---------------------------- |
| email     | String | Yes      | Email đã nhận OTP            |
| otpCode   | String | Yes      | Mã OTP 6 chữ số              |
| password  | String | Yes      | Mật khẩu (gửi lại từ bước 1) |
| username  | String | No       | Username (nếu có ở bước 1)   |
| firstName | String | No       | Tên (nếu có ở bước 1)        |
| lastName  | String | No       | Họ (nếu có ở bước 1)         |
| phone     | String | No       | SĐT (nếu có ở bước 1)        |

**Success Response (201 Created):**

```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "username": "john_doe",
      "firstName": "John",
      "lastName": "Doe",
      "phone": "0912345678",
      "role": "BUYER",
      "status": "ACTIVE",
      "provider": "LOCAL",
      "emailVerified": true,
      "createdAt": "2024-10-09T10:30:00",
      "updatedAt": "2024-10-09T10:30:00"
    }
  }
}
```

**Error Responses:**

**400 Bad Request** - OTP không hợp lệ hoặc hết hạn:

```json
{
  "success": false,
  "message": "Mã OTP không hợp lệ hoặc đã hết hạn",
  "data": {
    "message": "Mã OTP không hợp lệ hoặc đã hết hạn",
    "status": 400,
    "error": "Bad Request",
    "path": "/api/auth/verify-otp",
    "timestamp": "2024-10-09T10:35:00"
  }
}
```

**400 Bad Request** - OTP không chính xác:

```json
{
  "success": false,
  "message": "Mã OTP không chính xác",
  "data": {
    "message": "Mã OTP không chính xác",
    "status": 400,
    "error": "Bad Request",
    "path": "/api/auth/verify-otp",
    "timestamp": "2024-10-09T10:35:00"
  }
}
```

**400 Bad Request** - Quá số lần thử:

```json
{
  "success": false,
  "message": "Bạn đã nhập sai mã OTP quá số lần cho phép",
  "data": {
    "message": "Bạn đã nhập sai mã OTP quá số lần cho phép",
    "status": 400,
    "error": "Bad Request",
    "path": "/api/auth/verify-otp",
    "timestamp": "2024-10-09T10:35:00"
  }
}
```

---

### 3. Gửi lại OTP

Gửi lại mã OTP mới nếu mã cũ hết hạn hoặc chưa nhận được.

**Endpoint:** `POST /api/auth/resend-otp`

**Request Body:**

```json
{
  "email": "user@example.com",
  "otpType": "REGISTRATION"
}
```

**Request Fields:**

| Field   | Type   | Required | Valid Values                 |
| ------- | ------ | -------- | ---------------------------- |
| email   | String | Yes      | Email hợp lệ                 |
| otpType | String | Yes      | REGISTRATION, PASSWORD_RESET |

**Success Response (200 OK):**

```json
{
  "success": true,
  "message": "Mã OTP mới đã được gửi",
  "data": {
    "email": "user@example.com",
    "expiresInMinutes": 5
  }
}
```

**Error Responses:**

**400 Bad Request** - Resend quá nhanh (rate limiting):

```json
{
  "success": false,
  "message": "Vui lòng đợi 60 giây trước khi gửi lại mã OTP",
  "data": {
    "message": "Vui lòng đợi 60 giây trước khi gửi lại mã OTP",
    "status": 400,
    "error": "Bad Request",
    "path": "/api/auth/resend-otp",
    "timestamp": "2024-10-09T10:31:00"
  }
}
```

---

## Quy tắc nghiệp vụ

### OTP Rules

- **Thời gian hết hạn:** 5 phút
- **Số lần thử tối đa:** 5 lần
- **Cooldown gửi lại:** 60 giây
- **Format:** 6 chữ số (ví dụ: 123456)

### Password Rules

- **Độ dài tối thiểu:** 8 ký tự
- **Yêu cầu:**
  - Ít nhất 1 chữ hoa (A-Z)
  - Ít nhất 1 chữ thường (a-z)
  - Ít nhất 1 chữ số (0-9)

### Email Rules

- Format email hợp lệ
- Unique per provider (LOCAL, GOOGLE, FACEBOOK, GITHUB)
- Tự động verify sau khi xác thực OTP thành công

### Phone Rules

- Format: `0[3|5|7|8|9]XXXXXXXX`
- Ví dụ: 0912345678, 0987654321
- Unique trong hệ thống

### JWT Tokens

- **Access Token:**
  - Thời gian sống: 1 giờ (3600 giây)
  - Sử dụng trong header: `Authorization: Bearer <token>`
- **Refresh Token:**
  - Thời gian sống: 7 ngày
  - Dùng để lấy access token mới

---

## Ví dụ sử dụng với cURL

### 1. Đăng ký - Gửi OTP

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "MyPassword123",
    "username": "john_doe",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "0912345678"
  }'
```

### 2. Xác thực OTP

```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "otpCode": "123456",
    "password": "MyPassword123",
    "username": "john_doe",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "0912345678"
  }'
```

### 3. Gửi lại OTP

```bash
curl -X POST http://localhost:8080/api/auth/resend-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "otpType": "REGISTRATION"
  }'
```

### 4. Sử dụng Access Token

```bash
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Postman Collection

Bạn có thể import collection sau vào Postman để test API:

```json
{
  "info": {
    "name": "Sport Commerce - Auth API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Register - Send OTP",
      "request": {
        "method": "POST",
        "header": [{ "key": "Content-Type", "value": "application/json" }],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"test@example.com\",\n  \"password\": \"Test123456\",\n  \"username\": \"testuser\",\n  \"firstName\": \"Test\",\n  \"lastName\": \"User\",\n  \"phone\": \"0912345678\"\n}"
        },
        "url": { "raw": "http://localhost:8080/api/auth/register" }
      }
    },
    {
      "name": "2. Verify OTP",
      "request": {
        "method": "POST",
        "header": [{ "key": "Content-Type", "value": "application/json" }],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"test@example.com\",\n  \"otpCode\": \"123456\",\n  \"password\": \"Test123456\",\n  \"username\": \"testuser\",\n  \"firstName\": \"Test\",\n  \"lastName\": \"User\",\n  \"phone\": \"0912345678\"\n}"
        },
        "url": { "raw": "http://localhost:8080/api/auth/verify-otp" }
      }
    },
    {
      "name": "3. Resend OTP",
      "request": {
        "method": "POST",
        "header": [{ "key": "Content-Type", "value": "application/json" }],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"test@example.com\",\n  \"otpType\": \"REGISTRATION\"\n}"
        },
        "url": { "raw": "http://localhost:8080/api/auth/resend-otp" }
      }
    }
  ]
}
```

---

## Cấu hình môi trường

### Database Configuration

Cập nhật file `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sportcommerce
    username: root
    password: your_mysql_password
```

### Email Configuration

Sử dụng Gmail SMTP (cần App Password):

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-specific-password
```

**Lấy App Password từ Google:**

1. Truy cập: https://myaccount.google.com/security
2. Bật 2-Step Verification
3. Tạo App Password cho "Mail"
4. Copy password và điền vào config

### JWT Secret

Thay đổi JWT secret trong production:

```yaml
jwt:
  secret: your-super-secret-key-change-this-in-production-must-be-long-enough
```

---

## Database Schema

### Table: users

```sql
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) DEFAULT NULL,
  `username` VARCHAR(100) DEFAULT NULL,
  `first_name` VARCHAR(100) DEFAULT NULL,
  `last_name` VARCHAR(100) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `role` ENUM('BUYER', 'SELLER', 'ADMIN') NOT NULL DEFAULT 'BUYER',
  `status` ENUM('ACTIVE', 'SUSPENDED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
  `provider` ENUM('LOCAL', 'GOOGLE', 'FACEBOOK', 'GITHUB') NOT NULL DEFAULT 'LOCAL',
  `email_verified` BOOLEAN NOT NULL DEFAULT FALSE,
  `email_verified_at` DATETIME(6) DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email_provider` (`email`, `provider`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
);
```

### Table: otp_verifications

```sql
CREATE TABLE `otp_verifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `otp_code` VARCHAR(6) NOT NULL,
  `otp_type` ENUM('REGISTRATION', 'PASSWORD_RESET', 'EMAIL_CHANGE', 'LOGIN_2FA') NOT NULL,
  `verified` BOOLEAN NOT NULL DEFAULT FALSE,
  `attempts` INT NOT NULL DEFAULT 0,
  `max_attempts` INT NOT NULL DEFAULT 5,
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `expires_at` DATETIME(6) NOT NULL,
  `verified_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_email_type` (`email`, `otp_type`),
  INDEX `idx_expires_at` (`expires_at`)
);
```

### Table: refresh_tokens

```sql
CREATE TABLE `refresh_tokens` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `token` VARCHAR(255) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `expires_at` DATETIME(6) NOT NULL,
  `revoked` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  INDEX `idx_user_id` (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);
```

---

## Troubleshooting

### 1. Email không gửi được

- Kiểm tra username/password SMTP
- Đảm bảo đã bật "Less secure app access" hoặc dùng App Password
- Kiểm tra firewall/network cho port 587

### 2. JWT Token không hợp lệ

- Kiểm tra JWT secret có đủ dài (>= 256 bits)
- Đảm bảo token chưa hết hạn
- Format header: `Authorization: Bearer <token>`

### 3. Database connection error

- Kiểm tra MySQL đã chạy
- Verify database name, username, password
- Kiểm tra port 3306

### 4. OTP hết hạn quá nhanh

- Điều chỉnh `otp.expiration-minutes` trong application.yml
- Kiểm tra timezone server và database

---

## Security Best Practices

1. **HTTPS:** Sử dụng HTTPS trong production
2. **JWT Secret:** Thay đổi secret key mạnh trong production
3. **Password:** Đã được hash bằng BCrypt
4. **Rate Limiting:** OTP resend có cooldown 60 giây
5. **Email Verification:** Bắt buộc verify email qua OTP
6. **Max Attempts:** Giới hạn 5 lần thử OTP

---

## Support

Để được hỗ trợ, vui lòng liên hệ:

- Email: support@sportcommerce.com
- GitHub Issues: [Repository Link]

---

**Version:** 1.0.0  
**Last Updated:** October 9, 2025
