// ...existing code...
package com.PBL6.Ecommerce.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.exception.AddressNotFoundException;
import com.PBL6.Ecommerce.exception.BadRequestException;
import com.PBL6.Ecommerce.exception.CartItemNotFoundException;
import com.PBL6.Ecommerce.exception.CategoryInUseException;
import com.PBL6.Ecommerce.exception.CategoryNotFoundException;
import com.PBL6.Ecommerce.exception.DuplicateCategoryException;
import com.PBL6.Ecommerce.exception.DuplicateEmailException;
import com.PBL6.Ecommerce.exception.DuplicatePhoneException;
import com.PBL6.Ecommerce.exception.DuplicateSKUException;
import com.PBL6.Ecommerce.exception.ExpiredOtpException;
import com.PBL6.Ecommerce.exception.ExpiredRefreshTokenException;
import com.PBL6.Ecommerce.exception.ForbiddenException;
import com.PBL6.Ecommerce.exception.InvalidCredentialsException;
import com.PBL6.Ecommerce.exception.InvalidOrderStatusException;
import com.PBL6.Ecommerce.exception.InvalidOtpException;
import com.PBL6.Ecommerce.exception.InvalidProductDataException;
import com.PBL6.Ecommerce.exception.InvalidRefreshTokenException;
import com.PBL6.Ecommerce.exception.InvalidRoleException;
import com.PBL6.Ecommerce.exception.MoMoPaymentException;
import com.PBL6.Ecommerce.exception.NotFoundException;
import com.PBL6.Ecommerce.exception.OrderNotFoundException;
import com.PBL6.Ecommerce.exception.OtpNotVerifiedException;
import com.PBL6.Ecommerce.exception.PasswordMismatchException;
import com.PBL6.Ecommerce.exception.ProductHasReferencesException;
import com.PBL6.Ecommerce.exception.ProductNotFoundException;
import com.PBL6.Ecommerce.exception.ShopNotFoundException;
import com.PBL6.Ecommerce.exception.UnauthenticatedException;
import com.PBL6.Ecommerce.exception.UnauthorizedAddressAccessException;
import com.PBL6.Ecommerce.exception.UnauthorizedOrderAccessException;
import com.PBL6.Ecommerce.exception.UnauthorizedProductAccessException;
import com.PBL6.Ecommerce.exception.UnauthorizedUserActionException;
import com.PBL6.Ecommerce.exception.UserHasReferencesException;
import com.PBL6.Ecommerce.exception.UserNotActivatedException;
import com.PBL6.Ecommerce.exception.UserNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed", ex);
        var errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(f -> f.getField(), f -> f.getDefaultMessage()));
        ResponseDTO<Map<String, String>> response = new ResponseDTO<>(
            400, 
            "VALIDATION_FAILED", 
            "Validation error", 
            errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            404,
            "USER_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("Product not found", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            404,
            "PRODUCT_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleCartItemNotFound(CartItemNotFoundException ex) {
        log.warn("Cart item not found", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            404,
            "CART_ITEM_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleCategoryNotFound(CategoryNotFoundException ex) {
        log.warn("Category not found", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            404,
            "CATEGORY_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ResponseDTO<Object>> handleDuplicateCategory(DuplicateCategoryException ex) {
        log.warn("Duplicate category", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            409,
            "DUPLICATE_CATEGORY",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ResponseDTO<Object>> handleCategoryInUse(CategoryInUseException ex) {
        log.warn("Category in use", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            409,
            "CATEGORY_IN_USE",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            404,
            "ORDER_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnauthorizedOrderAccessException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUnauthorizedOrderAccess(UnauthorizedOrderAccessException ex) {
        log.warn("Unauthorized order access", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            403,
            "FORBIDDEN",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ResponseDTO<Object>> handleInvalidOrderStatus(InvalidOrderStatusException ex) {
        log.warn("Invalid order status", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            400,
            "INVALID_ORDER_STATUS",
            ex.getMessage(),
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ShopNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleShopNotFound(ShopNotFoundException ex) {
        log.warn("Shop not found", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            404,
            "SHOP_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleAddressNotFound(AddressNotFoundException ex) {
        log.warn("Address not found", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            404,
            "ADDRESS_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnauthorizedAddressAccessException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUnauthorizedAddressAccess(UnauthorizedAddressAccessException ex) {
        log.warn("Unauthorized address access", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            403,
            "FORBIDDEN",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(InvalidProductDataException.class)
    public ResponseEntity<ResponseDTO<Object>> handleInvalidProductData(InvalidProductDataException ex) {
        log.warn("Invalid product data", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            400,
            "INVALID_PRODUCT_DATA",
            ex.getMessage(),
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UnauthorizedProductAccessException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUnauthorizedProductAccess(UnauthorizedProductAccessException ex) {
        log.warn("Unauthorized product access", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            403,
            "FORBIDDEN",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(DuplicateSKUException.class)
    public ResponseEntity<ResponseDTO<Object>> handleDuplicateSKU(DuplicateSKUException ex) {
        log.warn("Duplicate SKU", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            409,
            "DUPLICATE_SKU",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ProductHasReferencesException.class)
    public ResponseEntity<ResponseDTO<Object>> handleProductHasReferences(ProductHasReferencesException ex) {
        log.warn("Product has references", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            409,
            "PRODUCT_HAS_REFERENCES",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO<Object>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            400,
            "BAD_REQUEST",
            ex.getMessage(),
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseDTO<Object>> handleConflict(IllegalStateException ex) {
        log.warn("Conflict", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            409,
            "CONFLICT",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ============= Authentication & Authorization Exceptions (Auth Module) =============
    
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ResponseDTO<Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid credentials", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            401,
            "INVALID_CREDENTIALS",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUnauthenticated(UnauthenticatedException ex) {
        log.warn("Unauthenticated access", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            401,
            "UNAUTHENTICATED",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ResponseDTO<Object>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        log.warn("Invalid refresh token", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            401,
            "INVALID_REFRESH_TOKEN",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ResponseEntity<ResponseDTO<Object>> handleExpiredRefreshToken(ExpiredRefreshTokenException ex) {
        log.warn("Expired refresh token", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            401,
            "EXPIRED_REFRESH_TOKEN",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserNotActivatedException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUserNotActivated(UserNotActivatedException ex) {
        log.warn("User not activated", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            403,
            "USER_NOT_ACTIVATED",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UnauthorizedUserActionException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUnauthorizedUserAction(UnauthorizedUserActionException ex) {
        log.warn("Unauthorized user action", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            403,
            "FORBIDDEN",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ResponseDTO<Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        log.warn("Duplicate email", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            409,
            "DUPLICATE_EMAIL",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<ResponseDTO<Object>> handleDuplicatePhone(DuplicatePhoneException ex) {
        log.warn("Duplicate phone number", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            409,
            "DUPLICATE_PHONE",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserHasReferencesException.class)
    public ResponseEntity<ResponseDTO<Object>> handleUserHasReferences(UserHasReferencesException ex) {
        log.warn("User has references", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            409,
            "USER_HAS_REFERENCES",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ResponseDTO<Object>> handleInvalidOtp(InvalidOtpException ex) {
        log.warn("Invalid OTP", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            400,
            "INVALID_OTP",
            ex.getMessage(),
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ExpiredOtpException.class)
    public ResponseEntity<ResponseDTO<Object>> handleExpiredOtp(ExpiredOtpException ex) {
        log.warn("Expired OTP", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            400,
            "EXPIRED_OTP",
            ex.getMessage(),
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(OtpNotVerifiedException.class)
    public ResponseEntity<ResponseDTO<Object>> handleOtpNotVerified(OtpNotVerifiedException ex) {
        log.warn("OTP not verified", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            400,
            "OTP_NOT_VERIFIED",
            ex.getMessage(),
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ResponseDTO<Object>> handlePasswordMismatch(PasswordMismatchException ex) {
        log.warn("Password mismatch", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            400,
            "PASSWORD_MISMATCH",
            ex.getMessage(),
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ResponseDTO<Object>> handleInvalidRole(InvalidRoleException ex) {
        log.warn("Invalid role", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            400,
            "INVALID_ROLE",
            ex.getMessage(),
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    // ============= Payment Exceptions (MoMo Integration) =============

    @ExceptionHandler(MoMoPaymentException.class)
    public ResponseEntity<ResponseDTO<Object>> handleMoMoPaymentException(MoMoPaymentException ex) {
        log.error("MoMo payment error", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            502,
            "PAYMENT_GATEWAY_ERROR",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleNotFoundException(NotFoundException ex) {
        log.error("Resource not found", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            404,
            "NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ResponseDTO<Object>> handleForbiddenException(ForbiddenException ex) {
        log.error("Access forbidden", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            403,
            "FORBIDDEN",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseDTO<Object>> handleBadRequestException(BadRequestException ex) {
        log.error("Bad request", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            400,
            "BAD_REQUEST",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Object>> handleAll(Exception ex) {
        log.error("Unhandled error", ex);
        ResponseDTO<Object> response = new ResponseDTO<>(
            500,
            "INTERNAL_SERVER_ERROR",
            ex.getMessage() == null ? "Unexpected error" : ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
// ...existing code...