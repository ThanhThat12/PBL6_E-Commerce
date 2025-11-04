package com.PBL6.Ecommerce.exception;

public class UserHasReferencesException extends RuntimeException {
    public UserHasReferencesException(String message) {
        super(message);
    }

    public UserHasReferencesException(String message, Throwable cause) {
        super(message, cause);
    }
}
