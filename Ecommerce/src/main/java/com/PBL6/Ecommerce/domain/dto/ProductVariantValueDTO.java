package com.PBL6.Ecommerce.domain.dto;

public class ProductVariantValueDTO {
    private Long id;
    private Long productAttributeId;
    private String value;
    private AttributeDTO productAttribute;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductAttributeId() { return productAttributeId; }
    public void setProductAttributeId(Long productAttributeId) { this.productAttributeId = productAttributeId; }
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    
    public AttributeDTO getProductAttribute() { return productAttribute; }
    public void setProductAttribute(AttributeDTO productAttribute) { this.productAttribute = productAttribute; }
}