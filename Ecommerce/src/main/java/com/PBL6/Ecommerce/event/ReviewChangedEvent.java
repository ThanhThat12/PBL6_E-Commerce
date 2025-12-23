package com.PBL6.Ecommerce.event;

import org.springframework.context.ApplicationEvent;

/**
 * Domain event published when a review is created, updated, or deleted.
 * Used to trigger asynchronous rating recalculation after transaction commit.
 */
public class ReviewChangedEvent extends ApplicationEvent {
    
    private final Long productId;
    private final String operation; // CREATE, UPDATE, DELETE
    
    public ReviewChangedEvent(Object source, Long productId, String operation) {
        super(source);
        this.productId = productId;
        this.operation = operation;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public String getOperation() {
        return operation;
    }
}
