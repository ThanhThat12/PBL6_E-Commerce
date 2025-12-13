package com.PBL6.Ecommerce.domain.dto.order;

import com.PBL6.Ecommerce.domain.entity.order.Refund;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RefundDTO {
    private Long id;
    private Long orderId;
    private String reason;
    private BigDecimal amount;
    private String status;
    private List<String> imageUrls;
    private String returnMethod;
    private Boolean requiresReturn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String shopAddress; // Shop address for return shipping
    
    // Order item details for display
    private OrderItemDTO orderItem;

    public RefundDTO() {
    }

    public RefundDTO(Refund refund) {
        this.id = refund.getId();
        this.orderId = refund.getOrder() != null ? refund.getOrder().getId() : null;
        this.reason = extractReason(refund.getReason());
        this.amount = refund.getAmount();
        this.status = refund.getStatus() != null ? refund.getStatus().name() : null;
        this.imageUrls = parseImageUrls(refund.getImageUrl());
        this.returnMethod = extractReturnMethod(refund.getReason());
        this.requiresReturn = refund.getRequiresReturn();
        this.createdAt = refund.getCreatedAt();
        this.updatedAt = refund.getUpdatedAt();
    }
    
    /**
     * Extract return method from reason string
     * Format: "Return Request - Product: SKU-6, Quantity: 1, Method: SHIP_BACK, Reason: Ko ổn, Images:"
     */
    private String extractReturnMethod(String fullReason) {
        if (fullReason == null) return null;
        
        if (fullReason.contains("Method: ")) {
            int methodStart = fullReason.indexOf("Method: ") + 8;
            int methodEnd = fullReason.indexOf(",", methodStart);
            if (methodEnd > methodStart) {
                return fullReason.substring(methodStart, methodEnd).trim();
            }
        }
        
        return null;
    }
    
    /**
     * Extract original reason from concatenated reason string
     * Format: "Return Request - Product: SKU-6, Quantity: 1, Method: SHIP_BACK, Reason: Ko ổn, Images:"
     */
    private String extractReason(String fullReason) {
        if (fullReason == null) return null;
        
        // Check if reason contains "Reason: " pattern (from return request)
        if (fullReason.contains("Reason: ")) {
            int reasonStart = fullReason.indexOf("Reason: ") + 8;
            int reasonEnd = fullReason.indexOf(", Images:", reasonStart);
            if (reasonEnd == -1) {
                reasonEnd = fullReason.length();
            }
            if (reasonEnd > reasonStart) {
                return fullReason.substring(reasonStart, reasonEnd).trim();
            }
        }
        
        // If reason contains appended info like "[Lý do từ chối]:" or "[Kết quả kiểm tra]:"
        // Extract only the original reason
        int rejectIndex = fullReason.indexOf("\n[Lý do từ chối]:");
        int checkIndex = fullReason.indexOf("\n[Kết quả kiểm tra]:");
        
        if (rejectIndex > 0) {
            return fullReason.substring(0, rejectIndex).trim();
        }
        if (checkIndex > 0) {
            return fullReason.substring(0, checkIndex).trim();
        }
        
        return fullReason;
    }
    
    /**
     * Parse imageUrl string (JSON array or single URL) to List
     */
    private List<String> parseImageUrls(String imageUrl) {
        List<String> urls = new ArrayList<>();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return urls;
        }
        
        // Try to parse as JSON array
        if (imageUrl.trim().startsWith("[")) {
            try {
                // Simple parsing for JSON array of strings
                String content = imageUrl.trim().substring(1, imageUrl.length() - 1);
                String[] parts = content.split(",");
                for (String part : parts) {
                    String url = part.trim().replace("\"", "");
                    if (!url.isEmpty()) {
                        urls.add(url);
                    }
                }
            } catch (Exception e) {
                // If parsing fails, treat as single URL
                urls.add(imageUrl);
            }
        } else {
            // Single URL
            urls.add(imageUrl);
        }
        
        return urls;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getReturnMethod() {
        return returnMethod;
    }

    public void setReturnMethod(String returnMethod) {
        this.returnMethod = returnMethod;
    }

    public Boolean getRequiresReturn() {
        return requiresReturn;
    }

    public void setRequiresReturn(Boolean requiresReturn) {
        this.requiresReturn = requiresReturn;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OrderItemDTO getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItemDTO orderItem) {
        this.orderItem = orderItem;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public static class OrderItemDTO {
        private Long id;
        private String productName;
        private String productImage;
        private String variantName;
        private Integer quantity;
        private BigDecimal price;

        public OrderItemDTO() {
        }

        public OrderItemDTO(Long id, String productName, String productImage, String variantName, Integer quantity, BigDecimal price) {
            this.id = id;
            this.productName = productName;
            this.productImage = productImage;
            this.variantName = variantName;
            this.quantity = quantity;
            this.price = price;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductImage() {
            return productImage;
        }

        public void setProductImage(String productImage) {
            this.productImage = productImage;
        }

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
