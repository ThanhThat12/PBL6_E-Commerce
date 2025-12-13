package com.PBL6.Ecommerce.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartDTO {
    private String period;      // "Th7", "Th8", "Th9"...
    private BigDecimal revenue; // Doanh thu
    private Long orderCount;    // Số đơn hàng
}
