package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for seller reply to review
 */
public class SellerReplyRequestDTO {
    
    @NotBlank(message = "Seller response không được trống")
    @Size(max = 500, message = "Seller response không được vượt quá 500 ký tự")
    private String sellerResponse;

    // Constructors
    public SellerReplyRequestDTO() {}

    public SellerReplyRequestDTO(String sellerResponse) {
        this.sellerResponse = sellerResponse;
    }

    // Getters and Setters
    public String getSellerResponse() {
        return sellerResponse;
    }

    public void setSellerResponse(String sellerResponse) {
        this.sellerResponse = sellerResponse;
    }
}