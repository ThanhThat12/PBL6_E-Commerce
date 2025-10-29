package com.PBL6.Ecommerce.exception;

public class UnauthorizedOrderAccessException extends RuntimeException {
    public UnauthorizedOrderAccessException(Long orderId) {
        super("Bạn không có quyền truy cập đơn hàng ID: " + orderId);
    }
    
    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}
