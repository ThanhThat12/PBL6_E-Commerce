#!/bin/bash

# Cloudinary API Test Script
# Version: 1.0.0
# Date: November 4, 2025

echo "========================================"
echo "🧪 CLOUDINARY API TEST SUITE"
echo "========================================"
echo ""

# Configuration
BASE_URL="http://localhost:8080/api"
JWT_TOKEN=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print test result
print_test_result() {
    local test_name="$1"
    local result="$2"
    local details="$3"

    ((TOTAL_TESTS++))
    if [ "$result" = "PASS" ]; then
        ((PASSED_TESTS++))
        echo -e "${GREEN}✅ PASS${NC} - $test_name"
    else
        ((FAILED_TESTS++))
        echo -e "${RED}❌ FAIL${NC} - $test_name"
        if [ -n "$details" ]; then
            echo -e "${RED}   Details: $details${NC}"
        fi
    fi
}

# Function to login and get JWT token
login_test() {
    echo -e "${BLUE}🔐 Testing Authentication...${NC}"

    # Test BUYER login
    echo "Testing BUYER login..."
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"lmao","password":"123456"}')

    if echo "$LOGIN_RESPONSE" | grep -q "access_token"; then
        JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
        print_test_result "BUYER Login" "PASS"
    else
        print_test_result "BUYER Login" "FAIL" "Invalid response: $LOGIN_RESPONSE"
        return 1
    fi

    # Test SELLER login
    echo "Testing SELLER login..."
    SELLER_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"testsportsshop","password":"123456"}')

    if echo "$SELLER_LOGIN" | grep -q "access_token"; then
        SELLER_TOKEN=$(echo "$SELLER_LOGIN" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
        print_test_result "SELLER Login" "PASS"
    else
        print_test_result "SELLER Login" "FAIL" "Invalid response: $SELLER_LOGIN"
        return 1
    fi

    echo ""
    return 0
}

# Function to test avatar upload
test_avatar_upload() {
    echo -e "${BLUE}📸 Testing Avatar Upload...${NC}"

    # Create a small test image (1x1 pixel PNG)
    echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==" | base64 -d > test_avatar.png

    # Test successful upload
    echo "Testing successful avatar upload..."
    UPLOAD_RESPONSE=$(curl -s -X POST "$BASE_URL/upload/avatar" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -F "file=@test_avatar.png")

    if echo "$UPLOAD_RESPONSE" | grep -q '"url"' && echo "$UPLOAD_RESPONSE" | grep -q "cloudinary"; then
        AVATAR_URL=$(echo "$UPLOAD_RESPONSE" | grep -o '"url":"[^"]*' | cut -d'"' -f4)
        print_test_result "Avatar Upload Success" "PASS"
    else
        print_test_result "Avatar Upload Success" "FAIL" "Response: $UPLOAD_RESPONSE"
    fi

    # Test upload without file
    echo "Testing avatar upload without file..."
    NO_FILE_RESPONSE=$(curl -s -X POST "$BASE_URL/upload/avatar" \
        -H "Authorization: Bearer $JWT_TOKEN")

    if echo "$NO_FILE_RESPONSE" | grep -q '"status":400'; then
        print_test_result "Avatar Upload - No File (400)" "PASS"
    else
        print_test_result "Avatar Upload - No File (400)" "FAIL" "Response: $NO_FILE_RESPONSE"
    fi

    # Test upload without auth
    echo "Testing avatar upload without authentication..."
    NO_AUTH_RESPONSE=$(curl -s -X POST "$BASE_URL/upload/avatar" \
        -F "file=@test_avatar.png")

    if echo "$NO_AUTH_RESPONSE" | grep -q '"status":401'; then
        print_test_result "Avatar Upload - No Auth (401)" "PASS"
    else
        print_test_result "Avatar Upload - No Auth (401)" "FAIL" "Response: $NO_AUTH_RESPONSE"
    fi

    # Cleanup
    rm -f test_avatar.png
    echo ""
}

# Function to test product upload
test_product_upload() {
    echo -e "${BLUE}🛍️ Testing Product Image Upload...${NC}"

    # Create test image
    echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==" | base64 -d > test_product.png

    # Test successful upload with SELLER token
    echo "Testing product upload with SELLER..."
    PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/upload/product?productId=1" \
        -H "Authorization: Bearer $SELLER_TOKEN" \
        -F "file=@test_product.png")

    if echo "$PRODUCT_RESPONSE" | grep -q '"url"' && echo "$PRODUCT_RESPONSE" | grep -q "cloudinary"; then
        PRODUCT_URL=$(echo "$PRODUCT_RESPONSE" | grep -o '"url":"[^"]*' | cut -d'"' -f4)
        print_test_result "Product Upload Success" "PASS"
    else
        print_test_result "Product Upload Success" "FAIL" "Response: $PRODUCT_RESPONSE"
    fi

    # Test BUYER access denied
    echo "Testing product upload with BUYER (should fail)..."
    BUYER_PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/upload/product" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -F "file=@test_product.png")

    if echo "$BUYER_PRODUCT_RESPONSE" | grep -q '"status":403'; then
        print_test_result "Product Upload - BUYER Denied (403)" "PASS"
    else
        print_test_result "Product Upload - BUYER Denied (403)" "FAIL" "Response: $BUYER_PRODUCT_RESPONSE"
    fi

    # Cleanup
    rm -f test_product.png
    echo ""
}

# Function to test review upload
test_review_upload() {
    echo -e "${BLUE}⭐ Testing Review Image Upload...${NC}"

    # Create test images
    echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==" | base64 -d > review1.png
    echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==" | base64 -d > review2.png

    # Test single review upload
    echo "Testing single review upload..."
    SINGLE_REVIEW_RESPONSE=$(curl -s -X POST "$BASE_URL/upload/review" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -F "file=@review1.png")

    if echo "$SINGLE_REVIEW_RESPONSE" | grep -q '"url"' && echo "$SINGLE_REVIEW_RESPONSE" | grep -q "cloudinary"; then
        REVIEW_URL=$(echo "$SINGLE_REVIEW_RESPONSE" | grep -o '"url":"[^"]*' | cut -d'"' -f4)
        print_test_result "Single Review Upload Success" "PASS"
    else
        print_test_result "Single Review Upload Success" "FAIL" "Response: $SINGLE_REVIEW_RESPONSE"
    fi

    # Test multiple review upload
    echo "Testing multiple review upload..."
    MULTI_REVIEW_RESPONSE=$(curl -s -X POST "$BASE_URL/upload/reviews" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -F "files=@review1.png" \
        -F "files=@review2.png")

    if echo "$MULTI_REVIEW_RESPONSE" | grep -q '"urls"' && echo "$MULTI_REVIEW_RESPONSE" | grep -q "cloudinary"; then
        print_test_result "Multiple Review Upload Success" "PASS"
    else
        print_test_result "Multiple Review Upload Success" "FAIL" "Response: $MULTI_REVIEW_RESPONSE"
    fi

    # Test too many files
    echo "Testing too many review files (6 files)..."
    # Create additional test files
    for i in 3 4 5 6; do
        echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==" | base64 -d > "review$i.png"
    done

    TOO_MANY_RESPONSE=$(curl -s -X POST "$BASE_URL/upload/reviews" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -F "files=@review1.png" \
        -F "files=@review2.png" \
        -F "files=@review3.png" \
        -F "files=@review4.png" \
        -F "files=@review5.png" \
        -F "files=@review6.png")

    if echo "$TOO_MANY_RESPONSE" | grep -q '"status":400'; then
        print_test_result "Review Upload - Too Many Files (400)" "PASS"
    else
        print_test_result "Review Upload - Too Many Files (400)" "FAIL" "Response: $TOO_MANY_RESPONSE"
    fi

    # Cleanup
    rm -f review*.png
    echo ""
}

# Function to test image deletion
test_image_deletion() {
    echo -e "${BLUE}🗑️ Testing Image Deletion...${NC}"

    # First upload an image to delete
    echo "Uploading test image for deletion..."
    echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==" | base64 -d > delete_test.png

    UPLOAD_FOR_DELETE=$(curl -s -X POST "$BASE_URL/upload/avatar" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -F "file=@delete_test.png")

    DELETE_URL=$(echo "$UPLOAD_FOR_DELETE" | grep -o '"url":"[^"]*' | cut -d'"' -f4)

    if [ -n "$DELETE_URL" ]; then
        # Test successful deletion
        echo "Testing successful deletion..."
        DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/upload/image" \
            -H "Authorization: Bearer $JWT_TOKEN" \
            -H "Content-Type: application/json" \
            -d "{\"imageUrl\":\"$DELETE_URL\"}")

        if echo "$DELETE_RESPONSE" | grep -q '"success":true'; then
            print_test_result "Image Deletion Success" "PASS"
        else
            print_test_result "Image Deletion Success" "FAIL" "Response: $DELETE_RESPONSE"
        fi

        # Test delete with invalid URL
        echo "Testing deletion with invalid URL..."
        INVALID_DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/upload/image" \
            -H "Authorization: Bearer $JWT_TOKEN" \
            -H "Content-Type: application/json" \
            -d '{"imageUrl":"invalid-url"}')

        if echo "$INVALID_DELETE_RESPONSE" | grep -q '"status":400'; then
            print_test_result "Image Deletion - Invalid URL (400)" "PASS"
        else
            print_test_result "Image Deletion - Invalid URL (400)" "FAIL" "Response: $INVALID_DELETE_RESPONSE"
        fi
    else
        print_test_result "Image Deletion Setup" "FAIL" "Could not upload test image"
    fi

    # Test delete without auth
    echo "Testing deletion without authentication..."
    NO_AUTH_DELETE=$(curl -s -X DELETE "$BASE_URL/upload/image" \
        -H "Content-Type: application/json" \
        -d '{"imageUrl":"https://example.com/image.jpg"}')

    if echo "$NO_AUTH_DELETE" | grep -q '"status":401'; then
        print_test_result "Image Deletion - No Auth (401)" "PASS"
    else
        print_test_result "Image Deletion - No Auth (401)" "FAIL" "Response: $NO_AUTH_DELETE"
    fi

    # Cleanup
    rm -f delete_test.png
    echo ""
}

# Function to print test summary
print_summary() {
    echo "========================================"
    echo "📊 TEST SUMMARY"
    echo "========================================"
    echo "Total Tests: $TOTAL_TESTS"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"

    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "${GREEN}🎉 ALL TESTS PASSED!${NC}"
        echo "✅ Cloudinary API is working correctly"
    else
        echo -e "${RED}⚠️  SOME TESTS FAILED${NC}"
        echo "❌ Please check the failed tests above"
    fi

    echo ""
    echo "========================================"
}

# Main test execution
main() {
    echo "Starting Cloudinary API Test Suite..."
    echo "Base URL: $BASE_URL"
    echo ""

    # Check if server is running
    echo "Checking if server is running..."
    SERVER_CHECK=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/auth/login")

    if [ "$SERVER_CHECK" != "405" ] && [ "$SERVER_CHECK" != "400" ]; then
        echo -e "${RED}❌ Server is not running or not accessible at $BASE_URL${NC}"
        echo "Please start the Spring Boot application first."
        exit 1
    fi

    echo -e "${GREEN}✅ Server is running${NC}"
    echo ""

    # Run all tests
    login_test
    if [ $? -eq 0 ]; then
        test_avatar_upload
        test_product_upload
        test_review_upload
        test_image_deletion
    else
        echo -e "${RED}❌ Cannot proceed with tests due to authentication failure${NC}"
    fi

    # Print summary
    print_summary
}

# Run main function
main