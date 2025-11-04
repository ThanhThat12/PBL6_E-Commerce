package com.PBL6.Ecommerce.exception;

public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException(String message) {
        super(message);
    }

    public InvalidRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
