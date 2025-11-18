package com.PBL6.Ecommerce.domain.dto.admin;

public class AdminStatsDTO {
    private long totalAdmins;
    private long activeAdmins;
    private long inactiveAdmins;

    public AdminStatsDTO() {
    }

    public AdminStatsDTO(long totalAdmins, long activeAdmins, long inactiveAdmins) {
        this.totalAdmins = totalAdmins;
        this.activeAdmins = activeAdmins;
        this.inactiveAdmins = inactiveAdmins;
    }

    // Getters and Setters
    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public long getActiveAdmins() {
        return activeAdmins;
    }

    public void setActiveAdmins(long activeAdmins) {
        this.activeAdmins = activeAdmins;
    }

    public long getInactiveAdmins() {
        return inactiveAdmins;
    }

    public void setInactiveAdmins(long inactiveAdmins) {
        this.inactiveAdmins = inactiveAdmins;
    }
}
