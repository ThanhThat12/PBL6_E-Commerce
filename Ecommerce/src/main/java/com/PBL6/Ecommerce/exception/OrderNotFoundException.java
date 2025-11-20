package com.PBL6.Ecommerce.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super("Không tìm thấy đơn hàng với ID: " + orderId);
    }
    
    public OrderNotFoundException(String message) {
        super(message);
    }
}
