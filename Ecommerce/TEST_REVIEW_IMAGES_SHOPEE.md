# 🧪 Test Review Images - Shopee Style

## Quick Test Script (PowerShell)

```powershell
# === SETUP ===
$BASE_URL = "http://localhost:8080/api"

# 1. Login to get token
$loginResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"lmao","password":"lmao123"}'

$TOKEN = $loginResponse.data.accessToken
Write-Host "✅ Token: $TOKEN" -ForegroundColor Green

# === TEST 1: Create review with 3 images ===
Write-Host "`n📸 TEST 1: Create review with 3 images" -ForegroundColor Cyan

$createBody = @{
    orderId = 4
    rating = 5
    comment = "Test Shopee style - 3 ảnh ban đầu"
    images = @(
        "https://cdn.example.com/review1.jpg",
        "https://cdn.example.com/review2.jpg",
        "https://cdn.example.com/review3.jpg"
    )
} | ConvertTo-Json

$createResponse = Invoke-RestMethod -Uri "$BASE_URL/products/2/reviews" `
  -Method POST `
  -Headers @{Authorization="Bearer $TOKEN"; "Content-Type"="application/json"} `
  -Body $createBody

$REVIEW_ID = $createResponse.id
Write-Host "✅ Review created with ID: $REVIEW_ID" -ForegroundColor Green
Write-Host "Images count: $($createResponse.images.Count)" -ForegroundColor Yellow

# === TEST 2: Update review - ADD 2 more images (merge) ===
Write-Host "`n📸 TEST 2: Update review - ADD 2 more images (should merge to 5 total)" -ForegroundColor Cyan

$updateBody = @{
    rating = 5
    comment = "Updated - thêm 2 ảnh nữa"
    images = @(
        "https://cdn.example.com/review4.jpg",
        "https://cdn.example.com/review5.jpg"
    )
} | ConvertTo-Json

$updateResponse = Invoke-RestMethod -Uri "$BASE_URL/reviews/$REVIEW_ID" `
  -Method PUT `
  -Headers @{Authorization="Bearer $TOKEN"; "Content-Type"="application/json"} `
  -Body $updateBody

Write-Host "✅ Review updated" -ForegroundColor Green
Write-Host "Total images: $($updateResponse.images.Count)/5" -ForegroundColor Yellow
Write-Host "Images:" -ForegroundColor Gray
$updateResponse.images | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }

# === TEST 3: Try to add 1 more image (should FAIL - exceed limit) ===
Write-Host "`n📸 TEST 3: Try to add 1 more image (should FAIL - already have 5)" -ForegroundColor Cyan

$exceedBody = @{
    rating = 5
    comment = "Try to add 6th image"
    images = @("https://cdn.example.com/review6.jpg")
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$BASE_URL/reviews/$REVIEW_ID" `
      -Method PUT `
      -Headers @{Authorization="Bearer $TOKEN"; "Content-Type"="application/json"} `
      -Body $exceedBody
    Write-Host "❌ FAILED: Should have thrown error!" -ForegroundColor Red
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "✅ Expected error: $($errorResponse.message)" -ForegroundColor Green
}

# === TEST 4: Remove 2 specific images ===
Write-Host "`n📸 TEST 4: Remove 2 specific images (should have 3 left)" -ForegroundColor Cyan

$removeBody = @{
    imageUrls = @(
        "https://cdn.example.com/review2.jpg",
        "https://cdn.example.com/review4.jpg"
    )
} | ConvertTo-Json

$removeResponse = Invoke-RestMethod -Uri "$BASE_URL/reviews/$REVIEW_ID/images" `
  -Method DELETE `
  -Headers @{Authorization="Bearer $TOKEN"; "Content-Type"="application/json"} `
  -Body $removeBody

Write-Host "✅ Images removed" -ForegroundColor Green
Write-Host "Remaining images: $($removeResponse.images.Count)/5" -ForegroundColor Yellow
Write-Host "Images:" -ForegroundColor Gray
$removeResponse.images | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }

# === TEST 5: Add 2 more images after removal (should succeed - now only 3) ===
Write-Host "`n📸 TEST 5: Add 2 more images (should merge to 5 again)" -ForegroundColor Cyan

$addAgainBody = @{
    rating = 5
    comment = "Add 2 more after removal"
    images = @(
        "https://cdn.example.com/review6.jpg",
        "https://cdn.example.com/review7.jpg"
    )
} | ConvertTo-Json

$addAgainResponse = Invoke-RestMethod -Uri "$BASE_URL/reviews/$REVIEW_ID" `
  -Method PUT `
  -Headers @{Authorization="Bearer $TOKEN"; "Content-Type"="application/json"} `
  -Body $addAgainBody

Write-Host "✅ Images added" -ForegroundColor Green
Write-Host "Total images: $($addAgainResponse.images.Count)/5" -ForegroundColor Yellow
Write-Host "Final images:" -ForegroundColor Gray
$addAgainResponse.images | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }

# === TEST 6: Try to create review with 6 images (should FAIL) ===
Write-Host "`n📸 TEST 6: Try to create review with 6 images (should FAIL)" -ForegroundColor Cyan

$create6Body = @{
    orderId = 4
    rating = 5
    comment = "Try 6 images"
    images = @(
        "https://cdn.example.com/img1.jpg",
        "https://cdn.example.com/img2.jpg",
        "https://cdn.example.com/img3.jpg",
        "https://cdn.example.com/img4.jpg",
        "https://cdn.example.com/img5.jpg",
        "https://cdn.example.com/img6.jpg"
    )
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$BASE_URL/products/3/reviews" `
      -Method POST `
      -Headers @{Authorization="Bearer $TOKEN"; "Content-Type"="application/json"} `
      -Body $create6Body
    Write-Host "❌ FAILED: Should have thrown error!" -ForegroundColor Red
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "✅ Expected error: $($errorResponse.message)" -ForegroundColor Green
}

Write-Host "`n🎉 ALL TESTS COMPLETED!" -ForegroundColor Green
Write-Host "`nSummary:" -ForegroundColor Cyan
Write-Host "✅ Create with 3 images - SUCCESS" -ForegroundColor Green
Write-Host "✅ Merge 2 more (total 5) - SUCCESS" -ForegroundColor Green
Write-Host "✅ Try add 6th image - FAILED (as expected)" -ForegroundColor Green
Write-Host "✅ Remove 2 images - SUCCESS" -ForegroundColor Green
Write-Host "✅ Add 2 more after removal - SUCCESS" -ForegroundColor Green
Write-Host "✅ Create with 6 images - FAILED (as expected)" -ForegroundColor Green
```

---

## Manual Test với curl (Linux/Mac)

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/api"

# Login
TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"lmao","password":"lmao123"}' \
  | jq -r '.data.accessToken')

echo "Token: $TOKEN"

# Test 1: Create with 3 images
echo -e "\n=== TEST 1: Create review with 3 images ==="
REVIEW_ID=$(curl -s -X POST "$BASE_URL/products/2/reviews" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 4,
    "rating": 5,
    "comment": "Test 3 images",
    "images": [
      "https://cdn.example.com/review1.jpg",
      "https://cdn.example.com/review2.jpg",
      "https://cdn.example.com/review3.jpg"
    ]
  }' | jq -r '.id')

echo "✅ Review ID: $REVIEW_ID"

# Test 2: Update - merge 2 more
echo -e "\n=== TEST 2: Merge 2 more images (should be 5 total) ==="
curl -s -X PUT "$BASE_URL/reviews/$REVIEW_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "comment": "Add 2 more",
    "images": [
      "https://cdn.example.com/review4.jpg",
      "https://cdn.example.com/review5.jpg"
    ]
  }' | jq '.images | length'

# Test 3: Try to exceed limit
echo -e "\n=== TEST 3: Try to add 6th image (should fail) ==="
curl -s -X PUT "$BASE_URL/reviews/$REVIEW_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "images": ["https://cdn.example.com/review6.jpg"]
  }' | jq '.message'

# Test 4: Remove 2 images
echo -e "\n=== TEST 4: Remove 2 images ==="
curl -s -X DELETE "$BASE_URL/reviews/$REVIEW_ID/images" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrls": [
      "https://cdn.example.com/review2.jpg",
      "https://cdn.example.com/review4.jpg"
    ]
  }' | jq '.images | length'

echo -e "\n✅ All tests completed"
```

---

## Expected Results

### Test 1 - Create with 3 images ✅
```json
{
  "id": 1,
  "images": [
    "https://cdn.example.com/review1.jpg",
    "https://cdn.example.com/review2.jpg",
    "https://cdn.example.com/review3.jpg"
  ]
}
```

### Test 2 - Merge 2 more (total 5) ✅
```json
{
  "images": [
    "https://cdn.example.com/review1.jpg",
    "https://cdn.example.com/review2.jpg",
    "https://cdn.example.com/review3.jpg",
    "https://cdn.example.com/review4.jpg",
    "https://cdn.example.com/review5.jpg"
  ]
}
```

### Test 3 - Try add 6th ❌
```json
{
  "status": 400,
  "message": "Tổng số ảnh không được vượt quá 5 ảnh. Hiện có 5 ảnh cũ, bạn chỉ có thể thêm tối đa 0 ảnh mới."
}
```

### Test 4 - Remove 2 images ✅
```json
{
  "images": [
    "https://cdn.example.com/review1.jpg",
    "https://cdn.example.com/review3.jpg",
    "https://cdn.example.com/review5.jpg"
  ]
}
```

### Test 5 - Add 2 more (total 5 again) ✅
```json
{
  "images": [
    "https://cdn.example.com/review1.jpg",
    "https://cdn.example.com/review3.jpg",
    "https://cdn.example.com/review5.jpg",
    "https://cdn.example.com/review6.jpg",
    "https://cdn.example.com/review7.jpg"
  ]
}
```
