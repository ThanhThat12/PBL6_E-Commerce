package com.PBL6.Ecommerce.dto.seller;

/**
 * DTO for Image Upload Response
 * Used in POST /api/seller/shop/logo and /banner
 */
public class ImageUploadResponseDTO {
    
    private String imageUrl;
    private String message;
    
    public ImageUploadResponseDTO() {}

    public ImageUploadResponseDTO(String imageUrl, String message) {
        this.imageUrl = imageUrl;
        this.message = message;
    }

    // Getters and Setters
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
