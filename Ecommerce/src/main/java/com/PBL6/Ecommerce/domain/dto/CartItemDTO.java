package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartItemDTO {
    private Long id;                          // Cart item ID
    private Long variantId;                   // Product variant ID (renamed from productId)
    private Long productId;                   // Actual product ID (new field)
    private String productName;               // Product name
    private String productImage;              // Product main image URL (NEW)
    private String sku;                       // Variant SKU (NEW)
    private List<AttributeDTO> variantAttributes; // Variant attributes like Size, Color (NEW)
    private BigDecimal unitPrice;             // Unit price
    private int quantity;                     // Current quantity in cart
    private int stockAvailable;               // Stock available (NEW)
    private BigDecimal subTotal;              // Subtotal (unitPrice * quantity)

    public CartItemDTO() {}

    // Constructor for backward compatibility (deprecated)
    @Deprecated
    public CartItemDTO(Long id, Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subTotal) {
        this.id = id;
        this.variantId = productId; // Old field was actually variantId
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subTotal = subTotal;
    }

    // Full constructor with all fields
    public CartItemDTO(Long id, Long variantId, Long productId, String productName, String productImage, 
                       String sku, List<AttributeDTO> variantAttributes, BigDecimal unitPrice, 
                       int quantity, int stockAvailable, BigDecimal subTotal) {
        this.id = id;
        this.variantId = variantId;
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.sku = sku;
        this.variantAttributes = variantAttributes;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.stockAvailable = stockAvailable;
        this.subTotal = subTotal;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public List<AttributeDTO> getVariantAttributes() { return variantAttributes; }
    public void setVariantAttributes(List<AttributeDTO> variantAttributes) { this.variantAttributes = variantAttributes; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public int getStockAvailable() { return stockAvailable; }
    public void setStockAvailable(int stockAvailable) { this.stockAvailable = stockAvailable; }
    
    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
}