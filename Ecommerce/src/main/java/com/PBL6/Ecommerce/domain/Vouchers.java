package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
public class Vouchers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Khóa chính, tự tăng

    @Column(nullable = false, unique = true)
    private String code; // Mã voucher duy nhất

    @Column(nullable = false)
    private String description; // Mô tả voucher

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop; // Voucher thuộc shop nào

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // Kiểu giảm giá: % hoặc số tiền

    public enum DiscountType {
        PERCENTAGE, // Giảm theo phần trăm
        FIXED_AMOUNT // Giảm theo số tiền cố định
    }

    @Column(nullable = false)
    private BigDecimal discountValue; // Giá trị giảm (tùy theo discountType)

    @Column
    private BigDecimal minOrderValue; // Giá trị đơn hàng tối thiểu (có thể null)

    @Column(nullable = true)
    private BigDecimal maxDiscountAmount; // Mức giảm tối đa khi giảm theo %

    @Column(nullable = false)
    private LocalDateTime startDate; // Ngày bắt đầu hiệu lực

    @Column(nullable = false)
    private LocalDateTime endDate; // Ngày kết thúc hiệu lực

    @Column(nullable = false)
    private Integer usageLimit; // Tổng số lượt sử dụng được phép

    @Column(nullable = false)
    private Integer usedCount = 0; // Số lượt đã sử dụng

    @Column(nullable = false)
    private String applicableType; // Loại đối tượng áp dụng (ALL, TOP_BUYERS,...)

    @Column
    private Integer topBuyersCount; // Số lượng top buyer được áp dụng (nếu applicableType yêu cầu)

    @Column(nullable = false)
    private Boolean isActive = true; // Trạng thái voucher

    @Column
    private LocalDateTime createdAt = LocalDateTime.now(); // Thời gian tạo

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }

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

    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }

    public String getApplicableType() { return applicableType; }
    public void setApplicableType(String applicableType) { this.applicableType = applicableType; }

    public Integer getTopBuyersCount() { return topBuyersCount; }
    public void setTopBuyersCount(Integer topBuyersCount) { this.topBuyersCount = topBuyersCount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
