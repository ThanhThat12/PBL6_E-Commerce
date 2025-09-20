package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleLoginDTO {
    @NotBlank
    private String idToken;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    
}
