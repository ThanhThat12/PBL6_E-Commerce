# Seller Management API - Postman Collection

## Overview
This Postman collection contains APIs for seller management in the E-Commerce application, following Shopee-style buyer-to-seller upgrade system.

## APIs Included

### 🔐 Authentication
- **Login as Buyer** - Login with buyer account to test seller registration
- **Login as Seller** - Login with seller account to test shop management

### 🏪 Seller Registration & Shop Management
- **POST /api/seller/register** - Buyer upgrades to seller (auto-approval)
- **GET /api/seller/shop** - Get current seller's shop information
- **PUT /api/seller/shop** - Update seller's shop information

## Business Rules

### Seller Registration
- Only users with BUYER role can register as seller
- Phone number must be unique among all sellers
- Shop name must be unique
- Auto-approval (no admin review needed)
- User role automatically upgraded to SELLER

### Shop Management
- Only SELLER role users can access shop APIs
- Sellers can only manage their own shop

## Environment Variables Required

Set these in your Postman environment:
- `base_url` - API base URL (e.g., `http://localhost:8080`)
- `token` - JWT token (automatically set after login)
- `username` - Current logged in username

## Test Flow

1. **Login as Buyer**
   - Use buyer credentials
   - Token will be automatically saved

2. **Register as Seller**
   - Call `/api/seller/register` with shop details
   - User role upgraded to SELLER
   - Shop created with ACTIVE status

3. **Login as Seller**
   - Use same credentials (now SELLER role)
   - Token updated for seller operations

4. **Manage Shop**
   - Get shop info: `GET /api/seller/shop`
   - Update shop: `PUT /api/seller/shop`

## Error Scenarios to Test

- Register with existing seller phone number
- Register with existing shop name
- Register with non-buyer account
- Access shop APIs without SELLER role
- Update non-existent shop

## Sample Data

### Buyer Credentials
```json
{
  "username": "buyer1",
  "password": "buyer123"
}
```

### Seller Registration Request
```json
{
  "shopName": "My Awesome Shop",
  "shopDescription": "Best products in town",
  "shopPhone": "0987654321",
  "shopAddress": "123 Main Street, City"
}
```

### Shop Update Request
```json
{
  "name": "Updated Shop Name",
  "address": "456 New Address",
  "description": "Updated description",
  "status": "ACTIVE"
}
```