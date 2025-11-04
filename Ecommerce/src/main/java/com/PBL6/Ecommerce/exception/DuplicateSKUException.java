package com.PBL6.Ecommerce.exception;

public class DuplicateSKUException extends RuntimeException {
    public DuplicateSKUException(String message) {
        super(message);
    }

    public DuplicateSKUException(String message, Throwable cause) {
        super(message, cause);
    }
}
