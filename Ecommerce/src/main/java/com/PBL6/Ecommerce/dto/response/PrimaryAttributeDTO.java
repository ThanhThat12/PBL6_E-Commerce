package com.PBL6.Ecommerce.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for primary attribute information.
 * The primary attribute determines which attribute values should have variant-specific images.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryAttributeDTO {
    
    /**
     * Attribute ID (e.g., Color attribute ID)
     */
    private Long id;
    
    /**
     * Attribute name (e.g., "Color")
     */
    private String name;
    
    /**
     * List of available values for this attribute (e.g., ["Red", "Blue", "Green"])
     */
    private List<String> values;
}
