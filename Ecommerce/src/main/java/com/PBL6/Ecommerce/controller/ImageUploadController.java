package com.PBL6.Ecommerce.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.exception.BadRequestException;
import com.PBL6.Ecommerce.service.CloudinaryService;

/**
 * REST Controller for Image Upload
 * 
 * Endpoints:
 * - POST /api/upload/avatar - Upload avatar (BUYER/SELLER)
 * - POST /api/upload/product - Upload product image (SELLER only)
 * - POST /api/upload/review - Upload review image (BUYER only)
 * - POST /api/upload/reviews - Upload multiple review images (BUYER only)
 * - DELETE /api/upload/image - Delete image by URL
 */
@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * POST /api/upload/avatar
     * 
     * Upload avatar image (BUYER or SELLER)
     * 
     * Request:
     * - Content-Type: multipart/form-data
     * - Field name: file
     * 
     * Response:
     * {
     *   "url": "https://res.cloudinary.com/..."
     * }
     */
    @PostMapping("/avatar")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        // Get user ID from token (assuming 'sub' contains user ID)
        String username = jwt.getClaim("username");
        Long userId = jwt.getClaim("user_id"); // Adjust based on your JWT structure
        
        if (userId == null) {
            // Fallback: use username hash as ID
            userId = (long) username.hashCode();
        }
        
        String imageUrl = cloudinaryService.uploadAvatar(file, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("url", imageUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/upload/product
     * 
     * Upload product image (SELLER only)
     * 
     * Request:
     * - Content-Type: multipart/form-data
     * - Field name: file
     * - Optional: productId (if not provided, uses 0)
     */
    @PostMapping("/product")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Map<String, String>> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "productId", required = false, defaultValue = "0") Long productId
    ) {
        String imageUrl = cloudinaryService.uploadProductImage(file, productId);
        
        Map<String, String> response = new HashMap<>();
        response.put("url", imageUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/upload/review
     * 
     * Upload single review image (BUYER only)
     */
    @PostMapping("/review")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Map<String, String>> uploadReviewImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = jwt.getClaim("user_id");
        if (userId == null) {
            String username = jwt.getClaim("username");
            userId = (long) username.hashCode();
        }
        
        String imageUrl = cloudinaryService.uploadReviewImage(file, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("url", imageUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/upload/reviews
     * 
     * Upload multiple review images (BUYER only)
     * 
     * Request:
     * - Content-Type: multipart/form-data
     * - Field name: files[] (array of files)
     * 
     * Response:
     * {
     *   "urls": ["url1", "url2", ...]
     * }
     */
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Map<String, List<String>>> uploadReviewImages(
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (files == null || files.length == 0) {
            throw new BadRequestException("Không có file nào được chọn");
        }
        
        if (files.length > 5) {
            throw new BadRequestException("Chỉ được upload tối đa 5 ảnh");
        }
        
        Long userId = jwt.getClaim("user_id");
        if (userId == null) {
            String username = jwt.getClaim("username");
            userId = (long) username.hashCode();
        }
        
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String imageUrl = cloudinaryService.uploadReviewImage(file, userId);
            imageUrls.add(imageUrl);
        }
        
        Map<String, List<String>> response = new HashMap<>();
        response.put("urls", imageUrls);
        
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/upload/image
     * 
     * Delete image by URL
     * 
     * Request Body:
     * {
     *   "imageUrl": "https://res.cloudinary.com/..."
     * }
     */
    @DeleteMapping("/image")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteImage(
            @RequestBody Map<String, String> request
    ) {
        String imageUrl = request.get("imageUrl");
        
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new BadRequestException("URL ảnh không được để trống");
        }
        
        boolean deleted = cloudinaryService.deleteImage(imageUrl);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("message", deleted ? "Xóa ảnh thành công" : "Không thể xóa ảnh");
        
        return ResponseEntity.ok(response);
    }
}
