package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.*;

public class RegisterDTO {

    private String contact; // email hoặc sdt

    @NotBlank
    @Size(min = 1, max = 50)
    private String username;

    @NotBlank
    @Size(min = 4, max = 60)
    private String password;

    @NotBlank
    @Size(min = 4, max = 60)
    private String confirmPassword;

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}