package com.PBL6.Ecommerce.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductDTO {
    private Long productId;
    private String productName;
    private String categoryName;
    private String mainImage;
    private Long quantitySold;
    private BigDecimal totalRevenue;
    private String status;
}