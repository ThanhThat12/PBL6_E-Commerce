package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;

/**
 * Interface projection for native SQL queries returning admin product list data
 * Spring Data JPA will automatically implement this interface
 */
public interface AdminListProductProjection {
    Long getProductId();
    String getProductName();
    String getMainImage();
    String getCategoryName();
    BigDecimal getBasePrice();
    Long getTotalStock();
    Boolean getIsActive();
    Long getSales();
    Double getRating();
}
