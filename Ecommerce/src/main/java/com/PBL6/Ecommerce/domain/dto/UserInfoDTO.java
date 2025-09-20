package com.PBL6.Ecommerce.domain.dto;

public class UserInfoDTO {
    private Long id;
    private String email;
    private String Username;
    private String role;

    
    public UserInfoDTO(Long id, String email, String username, String role) {
        this.id = id;
        this.email = email;
        Username = username;
        this.role = role;
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
    public String getUserName() {
        return Username;
    }
    public void setUserName(String userName) {
        this.Username = userName;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
