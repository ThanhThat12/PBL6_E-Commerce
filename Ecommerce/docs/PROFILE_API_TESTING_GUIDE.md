# Profile API Testing Guide

## Quick Start Testing with Postman/Thunder Client

### Prerequisites
1. Start the Spring Boot application
2. Get a valid JWT token by logging in
3. Use the token in Authorization header for all requests below

---

## Test Scenarios

### 1. Get Current User Profile

**Request:**
```http
GET http://localhost:8080/api/user/profile
Authorization: Bearer {your_jwt_token}
```

**Expected Response (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "phoneNumber": "0123456789",
    "fullName": null,
    "role": "CUSTOMER",
    "avatarUrl": null,
    "activated": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

---

### 2. Update Profile - Success Case

**Request:**
```http
PUT http://localhost:8080/api/user/profile
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "fullName": "John Doe",
  "phoneNumber": "0987654321"
}
```

**Expected Response (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "phoneNumber": "0987654321",
    "fullName": "John Doe",
    "role": "CUSTOMER",
    "avatarUrl": null,
    "activated": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-15T14:30:25"
  }
}
```

---

### 3. Update Profile - Duplicate Email

**Request:**
```http
PUT http://localhost:8080/api/user/profile
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "email": "existing_user@example.com"
}
```

**Expected Response (400/409 Error):**
```json
{
  "statusCode": 400,
  "error": "DuplicateEmailException",
  "message": "Email already exists",
  "data": null
}
```

---

### 4. Update Profile - Validation Error

**Request:**
```http
PUT http://localhost:8080/api/user/profile
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "username": "ab",
  "email": "invalid-email"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "statusCode": 400,
  "error": "ValidationException",
  "message": "Validation failed: username must be between 3 and 50 characters, email must be valid",
  "data": null
}
```

---

### 5. Change Password - Success Case

**Request:**
```http
PUT http://localhost:8080/api/user/change-password
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "oldPassword": "Current@Password123",
  "newPassword": "NewSecure@Pass456",
  "confirmPassword": "NewSecure@Pass456"
}
```

**Expected Response (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Password changed successfully",
  "data": "Password has been updated"
}
```

---

### 6. Change Password - Wrong Old Password

**Request:**
```http
PUT http://localhost:8080/api/user/change-password
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "oldPassword": "WrongPassword123",
  "newPassword": "NewPassword456",
  "confirmPassword": "NewPassword456"
}
```

**Expected Response (403 Forbidden):**
```json
{
  "statusCode": 403,
  "error": "PasswordMismatchException",
  "message": "Old password is incorrect",
  "data": null
}
```

---

### 7. Change Password - Passwords Don't Match

**Request:**
```http
PUT http://localhost:8080/api/user/change-password
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "oldPassword": "Current@Password123",
  "newPassword": "NewPassword456",
  "confirmPassword": "DifferentPassword789"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "statusCode": 400,
  "error": "PasswordMismatchException",
  "message": "New password and confirmation do not match",
  "data": null
}
```

---

### 8. Change Password - Validation Error (Password Too Short)

**Request:**
```http
PUT http://localhost:8080/api/user/change-password
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "oldPassword": "Current@Password123",
  "newPassword": "12345",
  "confirmPassword": "12345"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "statusCode": 400,
  "error": "ValidationException",
  "message": "newPassword must be at least 6 characters",
  "data": null
}
```

---

### 9. Update Avatar

**Request:**
```http
PUT http://localhost:8080/api/user/avatar?avatarUrl=https://example.com/avatar.jpg
Authorization: Bearer {your_jwt_token}
```

**Expected Response (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Avatar updated successfully",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "phoneNumber": "0987654321",
    "fullName": "John Doe",
    "role": "CUSTOMER",
    "avatarUrl": "https://example.com/avatar.jpg",
    "activated": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-15T15:20:10"
  }
}
```

---

### 10. Unauthenticated Request

**Request:**
```http
GET http://localhost:8080/api/user/profile
(No Authorization header)
```

**Expected Response (401 Unauthorized):**
```json
{
  "statusCode": 401,
  "error": "UnauthenticatedException",
  "message": "Authentication required",
  "data": null
}
```

---

## Testing Checklist

### Functional Tests
- [ ] Can retrieve profile successfully
- [ ] Can update username
- [ ] Can update email
- [ ] Can update phone number
- [ ] Can update full name
- [ ] Can update all fields at once
- [ ] Can update only one field
- [ ] Can change password successfully
- [ ] Can update avatar URL
- [ ] createdAt timestamp is set automatically
- [ ] updatedAt timestamp updates on every change

### Validation Tests
- [ ] Username validation (3-50 chars)
- [ ] Email validation (valid email format)
- [ ] Phone validation (10-15 chars)
- [ ] Password validation (min 6 chars)
- [ ] Password confirmation matches

### Security Tests
- [ ] Cannot access without authentication
- [ ] Cannot use expired JWT token
- [ ] Cannot update with invalid JWT token
- [ ] Old password must be correct to change password
- [ ] Cannot update another user's profile

### Error Handling Tests
- [ ] Duplicate username returns proper error
- [ ] Duplicate email returns proper error
- [ ] Duplicate phone returns proper error
- [ ] Wrong old password returns proper error
- [ ] Password mismatch returns proper error
- [ ] Validation errors return proper messages

### Edge Cases
- [ ] Update with all null fields (should return current profile unchanged)
- [ ] Update with empty strings
- [ ] Very long avatar URL (500 chars limit)
- [ ] Unicode characters in fullName
- [ ] Special characters in username

---

## Database Verification Queries

After testing, verify the database changes:

```sql
-- Check if new columns exist
DESCRIBE users;

-- View a user record with new fields
SELECT id, username, email, full_name, avatar_url, created_at, updated_at
FROM users
WHERE id = 1;

-- Check updatedAt timestamp changes
SELECT id, username, updated_at
FROM users
WHERE id = 1;

-- After updating profile, run above query again and verify updated_at changed
```

---

## Performance Testing

Optional load testing:

```bash
# Test concurrent profile updates (requires Apache Bench or similar)
ab -n 1000 -c 10 -H "Authorization: Bearer {token}" \
   -H "Content-Type: application/json" \
   -p update_profile.json \
   http://localhost:8080/api/user/profile
```

---

## Common Issues & Solutions

### Issue 1: 401 Unauthorized
**Cause:** Token missing or expired  
**Solution:** Get a fresh token by logging in again

### Issue 2: 409 Conflict (Duplicate)
**Cause:** Username/email/phone already exists  
**Solution:** Use unique values or update different fields

### Issue 3: Validation Error
**Cause:** Input doesn't meet validation requirements  
**Solution:** Check field length, format (email), etc.

### Issue 4: Password Mismatch
**Cause:** Old password wrong or new passwords don't match  
**Solution:** Verify old password, ensure new password = confirm password

### Issue 5: 500 Internal Server Error
**Cause:** Database migration not run  
**Solution:** Run `migration_add_profile_fields.sql` first

---

## Next Steps After Testing

1. ✅ All tests pass → Proceed with frontend integration
2. ❌ Tests fail → Check logs in Spring Boot console
3. 🔄 Partial success → Debug specific failing endpoints

---

## Postman Collection Export

You can create a Postman collection with all these requests for easy testing:

1. Create new collection: "Profile API"
2. Add environment variable: `base_url` = `http://localhost:8080`
3. Add environment variable: `jwt_token` = `{your_token}`
4. Import all requests above
5. Use `{{base_url}}` and `{{jwt_token}}` in requests
