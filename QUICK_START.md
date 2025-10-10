# Quick Start Guide - Sport Commerce Registration System

## 🚀 Quick Setup (5 minutes)

### Step 1: Database Setup

```bash
# Install MySQL (if not installed)
# Then create database
mysql -u root -p
CREATE DATABASE sportcommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

### Step 2: Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Update these lines with your actual values:
spring.datasource.username=postgres
spring.datasource.password=YOUR_DB_PASSWORD

spring.mail.username=your-email@gmail.com
spring.mail.password=YOUR_APP_PASSWORD

jwt.secret=YOUR_LONG_SECRET_KEY_256_BITS
```

### Step 3: Run Application

```bash
mvn spring-boot:run
```

### Step 4: Test API

```bash
# Request OTP for Buyer
curl -X POST http://localhost:8080/api/auth/register/request-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "role": "BUYER"
  }'

# Check your email for OTP code

# Verify OTP (replace 123456 with actual OTP)
curl -X POST http://localhost:8080/api/auth/register/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otpCode": "123456",
    "password": "SecurePass123!"
  }'
```

## 📧 Gmail Setup for Sending OTP

1. Enable 2-Factor Authentication:

   - Go to: https://myaccount.google.com/security
   - Enable 2-Step Verification

2. Generate App Password:

   - Go to: https://myaccount.google.com/apppasswords
   - Select "Mail" and "Windows Computer"
   - Copy the 16-character password

3. Update `application.properties`:
   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=xxxx xxxx xxxx xxxx
   ```

## 🧪 Testing Workflow

### Test Buyer Registration:

1. **Request OTP** → Check email → Copy OTP
2. **Verify OTP** → Get JWT token
3. **Test complete!**

### Test Seller Registration:

1. **Request OTP** with all seller details
2. **Check email** → Copy OTP
3. **Verify OTP** → Get JWT token + seller info
4. **Test complete!**

### Test Error Cases:

- Request OTP twice within 1 minute (rate limit)
- Use wrong OTP 5 times (max attempts)
- Wait 6 minutes and use OTP (expired)
- Resend OTP before 60 seconds (cooldown)

## 🔍 Verify Database Tables

```sql
-- Connect to database
psql -U postgres -d sportcommerce

-- Check users table
SELECT * FROM users;

-- Check OTP verifications
SELECT email, otp_code, expires_at, verified FROM otp_verifications;
```

## 📊 Common Issues & Solutions

### Issue: Database Connection Failed

**Solution**:

- Check if MySQL is running: `mysqladmin ping -u root -p`
- Verify credentials in application.properties
- Ensure database exists: `psql -l`

### Issue: Email Not Sending

**Solution**:

- Use Gmail app password (not regular password)
- Check if 2FA is enabled
- Verify SMTP settings

### Issue: JWT Token Error

**Solution**:

- Ensure JWT secret is at least 256 bits (32 characters)
- Check for special characters in secret

### Issue: Port 8080 Already in Use

**Solution**:

```bash
# Find and kill process on port 8080 (Windows)
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=8081
```

## 📱 Testing with Postman

1. Import collection: `Sport-Commerce-API.postman_collection.json`
2. Update base URL if needed: `http://localhost:8080`
3. Run requests in order:
   - Request OTP → Verify OTP → Resend OTP

## 🎯 Next Steps

After successful registration:

1. ✅ User created with VERIFIED status
2. ✅ JWT token received
3. ✅ Welcome email sent
4. ✅ Ready for authentication

Now you can:

- Add login endpoint
- Add password reset flow
- Add OAuth2 providers (Google, Facebook, GitHub)
- Add user profile management
- Add role-based authorization

## 📖 API Endpoints Summary

| Endpoint                         | Method | Description           |
| -------------------------------- | ------ | --------------------- |
| `/api/auth/register/request-otp` | POST   | Request OTP           |
| `/api/auth/register/verify-otp`  | POST   | Verify OTP & Register |
| `/api/auth/register/resend-otp`  | POST   | Resend OTP            |

## 🔐 Security Checklist

- [x] Passwords hashed with BCrypt
- [x] OTP expires in 5 minutes
- [x] Rate limiting enabled
- [x] JWT tokens with expiry
- [x] Input validation
- [x] CSRF protection disabled for stateless API
- [x] SQL injection protection (JPA)

## 💡 Tips

- Use unique emails for testing
- Check spam folder for OTP emails
- OTP is 6 digits
- Password must be strong (8+ chars, 1 upper, 1 lower, 1 digit)
- Rate limit resets after 1 minute

## 📞 Support

If you encounter issues:

1. Check application logs
2. Verify database connection
3. Test email configuration
4. Review validation rules

Happy coding! 🎉
