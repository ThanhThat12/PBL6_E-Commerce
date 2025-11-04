package com.PBL6.Ecommerce.exception;

public class ShopNotFoundException extends RuntimeException {
    public ShopNotFoundException(Long userId) {
        super("Seller chưa có shop (User ID: " + userId + ")");
    }
    
    public ShopNotFoundException(String message) {
        super(message);
    }
}
