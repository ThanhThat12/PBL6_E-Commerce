package com.PBL6.Ecommerce.domain.dto;
import jakarta.validation.constraints.NotBlank;

public class FacebookLoginDTO {
    @NotBlank
     private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
