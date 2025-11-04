package com.PBL6.Ecommerce.exception;

public class AddressNotFoundException extends RuntimeException {
    private final Long addressId;

    public AddressNotFoundException(Long addressId) {
        super("Không tìm thấy địa chỉ với ID: " + addressId);
        this.addressId = addressId;
    }

    public Long getAddressId() {
        return addressId;
    }
}
