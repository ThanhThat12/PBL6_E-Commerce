package com.PBL6.Ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderCalculateRequest {
    
    private Long userId;
    private Long shopId;
    private List<OrderItemRequest> items;
    private String voucherCode;
    private Long addressId;
    private String shippingMethod;

    public static class OrderItemRequest {
        private Long productId;
        private Long variantId;
        private Integer quantity;
        private BigDecimal price;

        // Constructors
        public OrderItemRequest() {
        }

        public OrderItemRequest(Long productId, Long variantId, Integer quantity, BigDecimal price) {
            this.productId = productId;
            this.variantId = variantId;
            this.quantity = quantity;
            this.price = price;
        }

        // Getters and Setters
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Long getVariantId() {
            return variantId;
        }

        public void setVariantId(Long variantId) {
            this.variantId = variantId;
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

        @Override
        public String toString() {
            return "OrderItemRequest{" +
                    "productId=" + productId +
                    ", variantId=" + variantId +
                    ", quantity=" + quantity +
                    ", price=" + price +
                    '}';
        }
    }

    // Constructors
    public OrderCalculateRequest() {
    }

    public OrderCalculateRequest(Long userId, Long shopId, List<OrderItemRequest> items, 
                                String voucherCode, Long addressId, String shippingMethod) {
        this.userId = userId;
        this.shopId = shopId;
        this.items = items;
        this.voucherCode = voucherCode;
        this.addressId = addressId;
        this.shippingMethod = shippingMethod;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    @Override
    public String toString() {
        return "OrderCalculateRequest{" +
                "userId=" + userId +
                ", shopId=" + shopId +
                ", items=" + items +
                ", voucherCode='" + voucherCode + '\'' +
                ", addressId=" + addressId +
                ", shippingMethod='" + shippingMethod + '\'' +
                '}';
    }
}
