package com.PBL6.Ecommerce.dto.seller;

/**
 * DTO for Order Statistics by Status
 * Used in dashboard and order management
 */
public class OrderStatsDTO {
    private Long total;
    private Long pending;
    private Long processing;
    private Long completed;
    private Long cancelled;
    
    public OrderStatsDTO() {}
    
    public OrderStatsDTO(Long total, Long pending, Long processing, 
                        Long completed, Long cancelled) {
        this.total = total;
        this.pending = pending;
        this.processing = processing;
        this.completed = completed;
        this.cancelled = cancelled;
    }

    // Getters and Setters
    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPending() {
        return pending;
    }

    public void setPending(Long pending) {
        this.pending = pending;
    }

    public Long getProcessing() {
        return processing;
    }

    public void setProcessing(Long processing) {
        this.processing = processing;
    }

    public Long getCompleted() {
        return completed;
    }

    public void setCompleted(Long completed) {
        this.completed = completed;
    }

    public Long getCancelled() {
        return cancelled;
    }

    public void setCancelled(Long cancelled) {
        this.cancelled = cancelled;
    }
}
