package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import com.PBL6.Ecommerce.domain.Product.ProductStatus;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String mainImage; // thay vì image
    private BigDecimal basePrice; // thay vì price
    private ProductStatus status; // ✅ Đổi từ Boolean isActive sang ProductStatus
    private CategoryDTO category; // thay vì categoryName
    private String shopName; // có thể giữ hoặc đổi thành ShopDTO
    private List<ProductVariantDTO> variants; // thêm variants
    private List<ProductImageDTO> images; // thêm images

    // Constructor mặc định
    public ProductDTO() {}

    // Constructor với các fields cơ bản (backward compatibility)
    public ProductDTO(Long id, String name, String description, String image, BigDecimal price, 
                     Integer stock, String condition, String categoryName, String shopName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.mainImage = image;
        this.basePrice = price;
        this.shopName = shopName;
        // Tạo category object
        this.category = new CategoryDTO();
        this.category.setName(categoryName);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public CategoryDTO getCategory() { return category; }
    public void setCategory(CategoryDTO category) { this.category = category; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public List<ProductVariantDTO> getVariants() { return variants; }
    public void setVariants(List<ProductVariantDTO> variants) { this.variants = variants; }

    public List<ProductImageDTO> getImages() { return images; }
    public void setImages(List<ProductImageDTO> images) { this.images = images; }

    // Backward compatibility methods
    public String getImage() { return mainImage; }
    public void setImage(String image) { this.mainImage = image; }

    public BigDecimal getPrice() { return basePrice; }
    public void setPrice(BigDecimal price) { this.basePrice = price; }

    public String getCategoryName() { 
        return category != null ? category.getName() : null; 
    }
    public void setCategoryName(String categoryName) { 
        if (category == null) category = new CategoryDTO();
        category.setName(categoryName);
    }

    // Các fields cũ không còn dùng
    public Integer getStock() { 
        // Có thể trả về stock của variant đầu tiên
        if (variants != null && !variants.isEmpty()) {
            return variants.get(0).getStock();
        }
        return null; 
    }
    public void setStock(Integer stock) { /* deprecated */ }

    public String getCondition() { return null; } // deprecated
    public void setCondition(String condition) { /* deprecated */ }
}