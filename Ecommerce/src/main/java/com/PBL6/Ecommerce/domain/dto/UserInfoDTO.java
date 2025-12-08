package com.PBL6.Ecommerce.domain.dto;

public class UserInfoDTO {
    private Long id;
    private String email;
    private String username;
    private String role;
    private String fullName;

    
    public UserInfoDTO(Long id, String email, String username, String role) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
    }
    
    public UserInfoDTO(Long id, String email, String username, String role, String fullName) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
