package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartDTO {
    private Long id;
    private List<CartItemDTO> items;
    private BigDecimal total;

    public CartDTO() {}

    public CartDTO(Long id, List<CartItemDTO> items, BigDecimal total) {
        this.id = id;
        this.items = items;
        this.total = total;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public List<CartItemDTO> getItems() { return items; }
    public void setItems(List<CartItemDTO> items) { this.items = items; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}