package com.PBL6.Ecommerce.domain.dto;

/**
 * DTO for temporary image upload response (before review creation)
 * Used for POST /api/reviews/images/upload endpoint
 */
public class TempImageUploadResponseDTO {
    
    private String url;
    private String publicId;
    private Integer width;
    private Integer height;
    private Long size;
    private String message;
    
    public TempImageUploadResponseDTO() {}
    
    public TempImageUploadResponseDTO(String url, String publicId, Integer width, Integer height, Long size, String message) {
        this.url = url;
        this.publicId = publicId;
        this.width = width;
        this.height = height;
        this.size = size;
        this.message = message;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String url;
        private String publicId;
        private Integer width;
        private Integer height;
        private Long size;
        private String message;
        
        public Builder url(String url) { this.url = url; return this; }
        public Builder publicId(String publicId) { this.publicId = publicId; return this; }
        public Builder width(Integer width) { this.width = width; return this; }
        public Builder height(Integer height) { this.height = height; return this; }
        public Builder size(Long size) { this.size = size; return this; }
        public Builder message(String message) { this.message = message; return this; }
        
        public TempImageUploadResponseDTO build() {
            return new TempImageUploadResponseDTO(url, publicId, width, height, size, message);
        }
    }
    
    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
