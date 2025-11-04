package com.PBL6.Ecommerce.exception;

public class ExpiredRefreshTokenException extends RuntimeException {
    public ExpiredRefreshTokenException(String message) {
        super(message);
    }

    public ExpiredRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
