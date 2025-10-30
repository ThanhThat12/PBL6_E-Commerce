package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.dto.*;

public interface CheckoutService {
    /**
     * Calculate order total with voucher and shipping fee
     */
    OrderCalculateResponse calculateOrder(OrderCalculateRequest request, Long userId);
    
    /**
     * Create order with full transaction management
     */
    OrderResponse createOrder(CreateOrderRequest request, Long userId);
    
    /**
     * Handle payment callback from payment gateway
     */
    void handlePaymentCallback(PaymentCallbackRequest request);
}
