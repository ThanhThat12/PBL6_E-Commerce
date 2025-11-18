package com.PBL6.Ecommerce.domain.dto.admin;

public class AdminVoucherStatsDTO {
    private Long totalVouchers;
    private Long activeVouchers;
    private Long expiredVouchers;
    private Long usedVouchers;

    // Constructors
    public AdminVoucherStatsDTO() {}

    public AdminVoucherStatsDTO(Long totalVouchers, Long activeVouchers, 
                                Long expiredVouchers, Long usedVouchers) {
        this.totalVouchers = totalVouchers;
        this.activeVouchers = activeVouchers;
        this.expiredVouchers = expiredVouchers;
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

    public Long getExpiredVouchers() {
        return expiredVouchers;
    }

    public void setExpiredVouchers(Long expiredVouchers) {
        this.expiredVouchers = expiredVouchers;
    }

    public Long getUsedVouchers() {
        return usedVouchers;
    }

    public void setUsedVouchers(Long usedVouchers) {
        this.usedVouchers = usedVouchers;
    }
}
