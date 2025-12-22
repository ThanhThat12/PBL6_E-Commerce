package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đơn giản chỉ chứa thông tin cơ bản của sản phẩm
 * Dùng cho danh sách sản phẩm, trang shop, tìm kiếm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardDTO {
    private Long id;
    private String name;
    private String mainImage;
    private BigDecimal basePrice;
    private BigDecimal rating;
    private Integer soldCount;
    private Long shopId;
    private String shopName;
}
