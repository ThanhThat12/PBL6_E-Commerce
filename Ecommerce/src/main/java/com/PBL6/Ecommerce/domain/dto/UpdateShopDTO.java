package com.PBL6.Ecommerce.domain.dto;

/**
 * DTO để cập nhật thông tin shop
 * Chỉ cho phép cập nhật: name, address, description, status
 */
public class UpdateShopDTO {
    private String name;
    private String address;
    // Optional: allow selecting existing address id instead of plain string
    private Long pickupAddressId;
    private String description;
    private String status;

    public UpdateShopDTO() {
    }

    public UpdateShopDTO(String name, String address, String description, String status) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getPickupAddressId() {
        return pickupAddressId;
    }

    public void setPickupAddressId(Long pickupAddressId) {
        this.pickupAddressId = pickupAddressId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
