package com.PBL6.Ecommerce.exception;

public class AdminAccessDeniedException extends RuntimeException {
    public AdminAccessDeniedException(String message) {
        super(message);
    }
}
