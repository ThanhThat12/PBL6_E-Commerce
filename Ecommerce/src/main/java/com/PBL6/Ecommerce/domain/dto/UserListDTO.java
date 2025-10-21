package com.PBL6.Ecommerce.domain.dto;

public class UserListDTO {
    private Long id;
    private boolean activated;
    private String email;
    private String username;

    public UserListDTO() {
    }

    public UserListDTO(Long id, boolean activated, String email, String username) {
        this.id = id;
        this.activated = activated;
        this.email = email;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
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
}
