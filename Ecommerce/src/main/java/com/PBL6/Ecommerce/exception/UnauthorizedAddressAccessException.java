package com.PBL6.Ecommerce.exception;

public class UnauthorizedAddressAccessException extends RuntimeException {
    private final Long addressId;

    public UnauthorizedAddressAccessException(Long addressId) {
        super("Bạn không có quyền truy cập địa chỉ ID: " + addressId);
        this.addressId = addressId;
    }

    public Long getAddressId() {
        return addressId;
    }
}
