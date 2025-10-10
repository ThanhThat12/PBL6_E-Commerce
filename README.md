# Sport Commerce - User Registration System

This Spring Boot application implements a secure user registration system with OTP verification for both BUYER and SELLER roles.

## Features

- ✅ OTP-based registration for BUYER and SELLER
- ✅ Email verification with 5-minute OTP expiry
- ✅ Rate limiting (1 request/minute per email)
- ✅ Resend cooldown (60 seconds)
- ✅ Daily resend limit (5 resends per email)
- ✅ JWT authentication with access tokens
- ✅ BCrypt password hashing
- ✅ Role-based user management
- ✅ MySQL database
- ✅ Comprehensive validation

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Database**: MySQL
- **Security**: Spring Security + JWT
- **Email**: JavaMailSender (SMTP)
- **Cache**: Caffeine
- **Build Tool**: Maven
- **Java Version**: 21

## Prerequisites

Before running this application, ensure you have:

1. **Java 21** installed
2. **Maven** installed
3. **MySQL** database server running
4. **Gmail account** (or other SMTP service) for sending emails

## Database Setup

1. Install MySQL if not already installed

2. Create a new database:

```sql
CREATE DATABASE sportcommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. Update database credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sportcommerce?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

## Email Configuration

### Using Gmail:

1. Enable 2-Factor Authentication on your Gmail account

2. Generate an App-Specific Password:

   - Go to Google Account Settings → Security → 2-Step Verification → App passwords
   - Generate a new app password for "Mail"

3. Update email configuration in `application.properties`:

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-character-app-password
```

### Using Other SMTP Services:

Update the SMTP settings accordingly:

```properties
spring.mail.host=smtp.your-provider.com
spring.mail.port=587
spring.mail.username=your-email
spring.mail.password=your-password
```

## JWT Configuration

Update the JWT secret in `application.properties` (must be at least 256 bits):

```properties
jwt.secret=your-very-long-and-secure-secret-key-at-least-256-bits
```

## Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Build the project:

```bash
mvn clean install
```

4. Run the application:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Request OTP

**Endpoint**: `POST /api/auth/register/request-otp`

**For BUYER**:

```json
{
  "email": "buyer@example.com",
  "role": "BUYER"
}
```

**For SELLER**:

```json
{
  "email": "seller@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+84912345678",
  "shopName": "MyShop",
  "shopAddress": "123 Street, City, Country",
  "taxId": "TAX123456",
  "role": "SELLER"
}
```

**Response**:

```json
{
  "success": true,
  "message": "OTP has been sent to your email",
  "expiresIn": 300,
  "canResendAt": "2025-10-09T15:50:00Z"
}
```

### 2. Verify OTP

**Endpoint**: `POST /api/auth/register/verify-otp`

**Request**:

```json
{
  "email": "user@example.com",
  "otpCode": "123456",
  "password": "SecurePass123!"
}
```

**Response**:

```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "SELLER",
    "phone": "+84912345678",
    "shopName": "MyShop",
    "shopAddress": "123 Street, City",
    "taxId": "TAX123456",
    "emailVerificationStatus": "VERIFIED",
    "provider": "LOCAL"
  }
}
```

### 3. Resend OTP

**Endpoint**: `POST /api/auth/register/resend-otp`

**Request**:

```json
{
  "email": "user@example.com"
}
```

**Response**:

```json
{
  "success": true,
  "message": "New OTP has been sent",
  "expiresIn": 300,
  "canResendAt": "2025-10-09T15:51:00Z"
}
```

## Validation Rules

### Email

- Required
- Must be valid email format
- Must not already be registered

### Password

- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit

### Phone (SELLER only)

- Must match E.164 format: `^\+?[1-9]\d{1,14}$`
- Example: `+84912345678`

### Tax ID (SELLER only)

- 6-10 alphanumeric characters
- Pattern: `^[A-Z0-9]{6,10}$`
- Example: `TAX123456`

## Security Features

1. **Rate Limiting**: 1 request per minute per email
2. **Resend Cooldown**: 60 seconds between resend requests
3. **Daily Limit**: Maximum 5 OTP resends per day
4. **OTP Expiry**: OTPs expire after 5 minutes
5. **Max Attempts**: Maximum 5 OTP verification attempts
6. **Password Hashing**: BCrypt with default strength (10)
7. **JWT Tokens**: 1-hour expiry for access tokens

## Database Schema

### Users Table

- `id` (UUID, primary key)
- `email` (unique, not null)
- `first_name` (nullable)
- `last_name` (nullable)
- `password` (not null)
- `role` (BUYER/SELLER)
- `provider` (LOCAL/GOOGLE/FACEBOOK/GITHUB)
- `email_verification_status` (PENDING/VERIFIED)
- `status` (ACTIVE/INACTIVE)
- `phone` (nullable)
- `shop_name` (nullable)
- `shop_address` (nullable)
- `tax_id` (nullable)
- `created_at` (timestamp)
- `updated_at` (timestamp)

### OTP Verifications Table

- `id` (UUID, primary key)
- `email` (not null)
- `otp_code` (not null)
- `otp_type` (REGISTRATION/PASSWORD_RESET)
- `attempts` (integer, default 0)
- `verified` (boolean, default false)
- `created_at` (timestamp)
- `expires_at` (timestamp)
- `verified_at` (nullable)
- `additional_info` (TEXT/JSON)

## Error Handling

The application includes comprehensive error handling:

- **400 Bad Request**: Invalid input, validation errors
- **429 Too Many Requests**: Rate limit exceeded
- **500 Internal Server Error**: Unexpected errors

Example error response:

```json
{
  "message": "Email already registered"
}
```

## Testing

You can test the API using tools like:

- **Postman**
- **cURL**
- **Thunder Client** (VS Code extension)

Example cURL command:

```bash
curl -X POST http://localhost:8080/api/auth/register/request-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "role": "BUYER"
  }'
```

## Logging

The application uses SLF4J for logging. Logs include:

- OTP generation and verification events
- User registration events
- Email sending status
- Error traces

**Note**: OTP codes and passwords are never logged for security reasons.

## Production Considerations

Before deploying to production:

1. **Change JWT Secret**: Use a strong, randomly generated secret
2. **Update Database Credentials**: Use secure passwords
3. **Configure Email Service**: Use a production-grade email service
4. **Enable HTTPS**: Use SSL/TLS for all API endpoints
5. **Set up monitoring**: Configure application monitoring and alerts
6. **Review security**: Conduct security audit
7. **Environment Variables**: Move sensitive configs to environment variables

## Troubleshooting

### Database Connection Issues

- Verify MySQL is running
- Check database credentials
- Ensure database exists

### Email Not Sending

- Verify SMTP credentials
- Check firewall settings
- Ensure app-specific password is used (for Gmail)

### JWT Token Issues

- Ensure JWT secret is at least 256 bits
- Check token expiration settings

## License

This project is part of Sport Commerce application.

## Support

For issues or questions, please contact the development team.
