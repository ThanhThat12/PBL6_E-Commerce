package com.PBL6.Ecommerce.dto;

import java.util.List;

public class CartDTO {
    private Long userId;
    private List<CartItemDTO> items;
    private Double total;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<CartItemDTO> getItems() { return items; }
    public void setItems(List<CartItemDTO> items) { this.items = items; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
