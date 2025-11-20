package com.PBL6.Ecommerce.domain.dto.admin;

public class SellerStatsDTO {
    private Long totalSellers;      // Tổng số seller
    private Long activeSellers;     // Số seller đang active
    private Long pendingSellers;    // Số seller đang pending
    private Long inactiveSellers;   // Số seller bị inactive

    public SellerStatsDTO() {
    }

    public SellerStatsDTO(Long totalSellers, Long activeSellers, Long pendingSellers, Long inactiveSellers) {
        this.totalSellers = totalSellers;
        this.activeSellers = activeSellers;
        this.pendingSellers = pendingSellers;
        this.inactiveSellers = inactiveSellers;
    }

    // Getters and Setters
    public Long getTotalSellers() {
        return totalSellers;
    }

    public void setTotalSellers(Long totalSellers) {
        this.totalSellers = totalSellers;
    }

    public Long getActiveSellers() {
        return activeSellers;
    }

    public void setActiveSellers(Long activeSellers) {
        this.activeSellers = activeSellers;
    }

    public Long getPendingSellers() {
        return pendingSellers;
    }

    public void setPendingSellers(Long pendingSellers) {
        this.pendingSellers = pendingSellers;
    }

    public Long getInactiveSellers() {
        return inactiveSellers;
    }

    public void setInactiveSellers(Long inactiveSellers) {
        this.inactiveSellers = inactiveSellers;
    }
}
