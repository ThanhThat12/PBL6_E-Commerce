package com.PBL6.Ecommerce.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesByCategoryDTO {
    private String categoryName;
    private BigDecimal totalSales;
    private Long orderCount;
}