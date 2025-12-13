package com.PBL6.Ecommerce.domain.dto.request;

import com.PBL6.Ecommerce.validator.ValidImageFile;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Image Replace Request DTO
 */
@Data
public class ImageReplaceRequest {
    
    @ValidImageFile(message = "Invalid image file")
    private MultipartFile newFile;
}
