package com.PBL6.Ecommerce.domain.dto.admin;

/**
 * DTO for voucher statistics (Admin only)
 */
public class AdminVoucherStatsDTO {
    private Long totalVouchers;
    private Long activeVouchers;
    private Long usedVouchers;

    public AdminVoucherStatsDTO() {
    }

    public AdminVoucherStatsDTO(Long totalVouchers, Long activeVouchers, Long usedVouchers) {
        this.totalVouchers = totalVouchers;
        this.activeVouchers = activeVouchers;
        this.usedVouchers = usedVouchers;
    }

    // Getters and Setters
    public Long getTotalVouchers() {
        return totalVouchers;
    }

    public void setTotalVouchers(Long totalVouchers) {
        this.totalVouchers = totalVouchers;
    }

    public Long getActiveVouchers() {
        return activeVouchers;
    }

    public void setActiveVouchers(Long activeVouchers) {
        this.activeVouchers = activeVouchers;
    }

    public Long getUsedVouchers() {
        return usedVouchers;
    }

    public void setUsedVouchers(Long usedVouchers) {
        this.usedVouchers = usedVouchers;
    }
}
