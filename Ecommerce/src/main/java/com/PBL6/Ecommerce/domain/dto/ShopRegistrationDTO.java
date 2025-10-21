package com.PBL6.Ecommerce.domain.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ShopRegistrationDTO {
    @NotBlank(message = "Tên shop không được để trống")
    @Size(max = 255, message = "Tên shop không được vượt quá 255 ký tự")
    private String name;
    
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;
    
    private String description;
    
    // Constructors
    public ShopRegistrationDTO() {}
    
    public ShopRegistrationDTO(String name, String address, String description) {
        this.name = name;
        this.address = address;
        this.description = description;
    }
    
    // Getters and Setters
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
}
