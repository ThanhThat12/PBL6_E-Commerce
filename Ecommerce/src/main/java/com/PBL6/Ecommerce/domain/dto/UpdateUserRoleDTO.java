package com.PBL6.Ecommerce.domain.dto;

public class UpdateUserRoleDTO {
    private String role; // ADMIN, SELLER, BUYER

    public UpdateUserRoleDTO() {
    }

    public UpdateUserRoleDTO(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}