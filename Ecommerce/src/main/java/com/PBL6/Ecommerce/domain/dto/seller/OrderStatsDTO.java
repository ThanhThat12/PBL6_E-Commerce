package com.PBL6.Ecommerce.domain.dto.seller;

/**
 * DTO for order statistics by status - Phase 3
 * Used for seller dashboard to show order counts by status
 */
public class OrderStatsDTO {
    
    private long total;
    private long pending;
    private long processing;
    private long completed;
    private long cancelled;
    
    public OrderStatsDTO() {}
    
    public OrderStatsDTO(long total, long pending, long processing, long completed, long cancelled) {
        this.total = total;
        this.pending = pending;
        this.processing = processing;
        this.completed = completed;
        this.cancelled = cancelled;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getProcessing() {
        return processing;
    }

    public void setProcessing(long processing) {
        this.processing = processing;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getCancelled() {
        return cancelled;
    }

    public void setCancelled(long cancelled) {
        this.cancelled = cancelled;
    }
}
