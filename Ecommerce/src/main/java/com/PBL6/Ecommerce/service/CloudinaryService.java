package com.PBL6.Ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.PBL6.Ecommerce.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling file uploads to Cloudinary
 */
@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;
    
    /**
     * Upload avatar image to cloud storage
     * 
     * @param file Avatar image file
     * @param userId User ID for naming
     * @return URL of uploaded image
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        validateImageFile(file);
        
        try {
            return uploadImageWithTransformation(file, "profiles", 300, 300, "fill");
        } catch (IOException e) {
            throw new BadRequestException("Không thể upload avatar: " + e.getMessage());
        }
    }
    
    /**
     * Upload product image to cloud storage
     * 
     * @param file Product image file
     * @param productId Product ID for naming
     * @return URL of uploaded image
     */
    public String uploadProductImage(MultipartFile file, Long productId) {
        validateImageFile(file);
        
        try {
            return uploadImageWithTransformation(file, "products", 800, 800, "fit");
        } catch (IOException e) {
            throw new BadRequestException("Không thể upload hình sản phẩm: " + e.getMessage());
        }
    }
    
    /**
     * Upload review image to cloud storage
     * 
     * @param file Review image file
     * @param reviewId Review ID for naming
     * @return URL of uploaded image
     */
    public String uploadReviewImage(MultipartFile file, Long reviewId) {
        validateImageFile(file);
        
        try {
            return uploadImage(file, "reviews");
        } catch (IOException e) {
            throw new BadRequestException("Không thể upload hình review: " + e.getMessage());
        }
    }

    /**
     * Upload shop logo to cloud storage
     */
    public String uploadShopLogo(MultipartFile file, Long shopId) {
        validateImageFile(file);
        
        try {
            return uploadImageWithTransformation(file, "shops/logos", 400, 400, "fill");
        } catch (IOException e) {
            throw new BadRequestException("Không thể upload logo shop: " + e.getMessage());
        }
    }

    /**
     * Upload shop banner to cloud storage
     */
    public String uploadShopBanner(MultipartFile file, Long shopId) {
        validateImageFile(file);
        
        try {
            return uploadImageWithTransformation(file, "shops/banners", 1200, 400, "fill");
        } catch (IOException e) {
            throw new BadRequestException("Không thể upload banner shop: " + e.getMessage());
        }
    }

    /**
     * Upload image to Cloudinary (basic)
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String publicId = folder + "/" + UUID.randomUUID().toString();

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "folder", folder,
                        "resource_type", "image",
                        "overwrite", false,
                        "quality", "auto:good",
                        "fetch_format", "auto"
                ));

        return (String) uploadResult.get("secure_url");
    }

    /**
     * Upload image with transformation
     */
    public String uploadImageWithTransformation(
            MultipartFile file, 
            String folder, 
            Integer width, 
            Integer height, 
            String crop
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String publicId = folder + "/" + UUID.randomUUID().toString();

        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "overwrite", false,
                "quality", "auto:good",
                "fetch_format", "auto"
        );

        if (width != null && height != null && crop != null) {
            params.put("transformation", ObjectUtils.asMap(
                    "width", width,
                    "height", height,
                    "crop", crop
            ));
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Delete image from Cloudinary
     */
    public boolean deleteImage(String publicId) throws IOException {
        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return "ok".equals(result.get("result"));
    }

    /**
     * Extract public_id from Cloudinary URL
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String pathAfterUpload = parts[1];
            String withoutVersion = pathAfterUpload.replaceFirst("v\\d+/", "");
            int lastDot = withoutVersion.lastIndexOf('.');
            if (lastDot > 0) {
                return withoutVersion.substring(0, lastDot);
            }

            return withoutVersion;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Validate image file
     * 
     * @param file File to validate
     * @throws BadRequestException if validation fails
     */
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }
        
        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("Kích thước file không được vượt quá 10MB");
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Chỉ chấp nhận file hình ảnh");
        }
    }
}

