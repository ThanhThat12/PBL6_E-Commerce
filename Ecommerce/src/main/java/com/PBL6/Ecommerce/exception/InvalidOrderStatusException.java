package com.PBL6.Ecommerce.exception;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String status) {
        super("Trạng thái không hợp lệ: " + status + ". Chỉ chấp nhận: PENDING, PROCESSING, COMPLETED, CANCELLED");
    }
}
