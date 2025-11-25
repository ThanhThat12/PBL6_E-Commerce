package com.PBL6.Ecommerce.validator;

import com.PBL6.Ecommerce.util.ImageValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validator for @ValidImageFile annotation
 * Delegates actual validation to ImageValidationUtil
 */
@Component
@RequiredArgsConstructor
public class ImageFileValidator implements ConstraintValidator<ValidImageFile, MultipartFile> {

    private final ImageValidationUtil imageValidationUtil;
    private boolean required;

    @Override
    public void initialize(ValidImageFile constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // If not required and file is null/empty, validation passes
        if (!required && (file == null || file.isEmpty())) {
            return true;
        }

        // If required and file is null/empty, validation fails
        if (required && (file == null || file.isEmpty())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Image file is required")
                   .addConstraintViolation();
            return false;
        }

        // Validate image using utility
        try {
            imageValidationUtil.validateImage(file);
            return true;
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                   .addConstraintViolation();
            return false;
        }
    }
}
