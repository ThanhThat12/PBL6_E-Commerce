package com.PBL6.Ecommerce.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.CloudinaryService;

/**
 * File Upload Controller
 * Handles image uploads to Cloudinary for products, profiles, etc.
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    private final CloudinaryService cloudinaryService;
    
    public FileUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }
    
    /**
     * Upload single product image
     * POST /api/upload/product-image
     */
    @PostMapping("/product-image")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<String>> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "productId", required = false) Long productId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "File không được để trống", null, null)
                );
            }
            
            String imageUrl = productId != null 
                ? cloudinaryService.uploadProductImage(file, productId)
                : cloudinaryService.uploadProductImage(file, 0L); // Temporary upload
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Upload ảnh thành công", imageUrl)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Upload ảnh thất bại", null)
            );
        }
    }
    
    /**
     * Upload multiple product images
     * POST /api/upload/product-images
     */
    @PostMapping("/product-images")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<List<String>>> uploadProductImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "productId", required = false) Long productId) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "Không có file nào được chọn", null, null)
                );
            }
            
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String imageUrl = productId != null
                        ? cloudinaryService.uploadProductImage(file, productId)
                        : cloudinaryService.uploadProductImage(file, 0L);
                    imageUrls.add(imageUrl);
                }
            }
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Upload " + imageUrls.size() + " ảnh thành công", imageUrls)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Upload ảnh thất bại", null)
            );
        }
    }
    
    /**
     * Upload profile avatar
     * POST /api/upload/avatar
     */
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "File không được để trống", null, null)
                );
            }
            
            String imageUrl = cloudinaryService.uploadAvatar(file, 0L);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Upload avatar thành công", imageUrl)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Upload avatar thất bại", null)
            );
        }
    }
    
    /**
     * Upload single review image
     * POST /api/upload/review
     */
    @PostMapping("/review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<String>> uploadReviewImage(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "File không được để trống", null, null)
                );
            }
            
            String imageUrl = cloudinaryService.uploadReviewImage(file, 0L);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Upload ảnh review thành công", imageUrl)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Upload ảnh review thất bại", null)
            );
        }
    }
    
    /**
     * Upload multiple review images
     * POST /api/upload/reviews
     */
    @PostMapping("/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<List<String>>> uploadReviewImages(
            @RequestParam("files") List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "Không có file nào được chọn", null, null)
                );
            }
            
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadReviewImage(file, 0L);
                    imageUrls.add(imageUrl);
                }
            }
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Upload " + imageUrls.size() + " ảnh review thành công", imageUrls)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Upload ảnh review thất bại", null)
            );
        }
    }
    
    /**
     * Delete image from Cloudinary
     * DELETE /api/upload/image
     */
    @DeleteMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<String>> deleteImage(
            @RequestBody Map<String, String> requestBody) {
        try {
            String imageUrl = requestBody.get("imageUrl");
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "imageUrl không được để trống", null, null)
                );
            }
            
            String publicId = cloudinaryService.extractPublicId(imageUrl);
            if (publicId == null) {
                return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(400, "Không thể trích xuất public_id từ URL", null, null)
                );
            }
            
            boolean deleted = cloudinaryService.deleteImage(publicId);
            if (deleted) {
                return ResponseEntity.ok(
                    new ResponseDTO<>(200, null, "Xóa ảnh thành công", null)
                );
            } else {
                return ResponseEntity.status(500).body(
                    new ResponseDTO<>(500, "Không thể xóa ảnh từ Cloudinary", null, null)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ResponseDTO<>(500, e.getMessage(), "Xóa ảnh thất bại", null)
            );
        }
    }
}
