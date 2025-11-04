package com.PBL6.Ecommerce.exception;

public class UnauthorizedUserActionException extends RuntimeException {
    public UnauthorizedUserActionException(String message) {
        super(message);
    }

    public UnauthorizedUserActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
