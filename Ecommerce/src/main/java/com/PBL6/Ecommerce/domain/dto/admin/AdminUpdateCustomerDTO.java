package com.PBL6.Ecommerce.domain.dto.admin;

public class AdminUpdateCustomerDTO {
    private Boolean activated; // status: true = active, false = inactive
    private String username;
    private String email;
    private String phone;

    public AdminUpdateCustomerDTO() {
    }

    public AdminUpdateCustomerDTO(Boolean activated, String username, String email, String phone) {
        this.activated = activated;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
