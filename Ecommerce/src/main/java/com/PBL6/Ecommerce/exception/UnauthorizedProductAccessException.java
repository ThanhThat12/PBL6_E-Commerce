package com.PBL6.Ecommerce.exception;

public class UnauthorizedProductAccessException extends RuntimeException {
    public UnauthorizedProductAccessException(String message) {
        super(message);
    }

    public UnauthorizedProductAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
