package com.PBL6.Ecommerce.domain.dto;

import com.PBL6.Ecommerce.domain.Vouchers.DiscountType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CreateVoucherRequestDTO {
    
    @NotBlank(message = "Mã voucher không được để trống")
    @Size(min = 3, max = 50, message = "Mã voucher phải từ 3-50 ký tự")
    private String code;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu phải >= 0")
    private BigDecimal minOrderValue;

    @DecimalMin(value = "0.0", message = "Số tiền giảm tối đa phải >= 0")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @NotNull(message = "Số lần sử dụng tối đa không được để trống")
    @Min(value = 1, message = "Số lần sử dụng tối đa phải >= 1")
    private Integer usageLimit;

    @NotBlank(message = "Loại áp dụng không được để trống")
    @Pattern(regexp = "ALL|SPECIFIC_PRODUCTS|SPECIFIC_USERS|TOP_BUYERS", 
             message = "Loại áp dụng phải là ALL, SPECIFIC_PRODUCTS, SPECIFIC_USERS hoặc TOP_BUYERS")
    private String applicableType;

    @Min(value = 1, message = "Số lượng top buyer phải >= 1")
    private Integer topBuyersCount;

    private List<Long> productIds; // Danh sách ID sản phẩm (nếu applicableType = SPECIFIC_PRODUCTS)

    private List<Long> userIds; // Danh sách ID user (nếu applicableType = SPECIFIC_USERS)

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getMinOrderValue() { return minOrderValue; }
    public void setMinOrderValue(BigDecimal minOrderValue) { this.minOrderValue = minOrderValue; }

    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public String getApplicableType() { return applicableType; }
    public void setApplicableType(String applicableType) { this.applicableType = applicableType; }

    public Integer getTopBuyersCount() { return topBuyersCount; }
    public void setTopBuyersCount(Integer topBuyersCount) { this.topBuyersCount = topBuyersCount; }

    public List<Long> getProductIds() { return productIds; }
    public void setProductIds(List<Long> productIds) { this.productIds = productIds; }

    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
}
