package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.PaymentTransaction;
import com.PBL6.Ecommerce.dto.PaymentCallbackRequest;
import com.PBL6.Ecommerce.dto.PaymentResponseDTO;

import java.math.BigDecimal;

/**
 * Interface for checkout and payment services
 */
public interface CheckoutService {
    
    /**
     * Create MoMo payment for an order
     * 
     * @param orderId Order ID
     * @param amount Payment amount
     * @param orderInfo Order information/description
     * @return PaymentResponseDTO containing payment URL and details
     */
    PaymentResponseDTO createMomoPayment(Long orderId, BigDecimal amount, String orderInfo);
    
    /**
     * Create MoMo payment with custom/unique orderId
     * 
     * @param orderId Internal Order ID
     * @param amount Payment amount
     * @param orderInfo Order information/description
     * @param momoOrderId Custom orderId to send to MoMo (unique, no duplicates)
     * @return PaymentResponseDTO containing payment URL and details
     */
    PaymentResponseDTO createMomoPaymentWithCustomOrderId(Long orderId, BigDecimal amount, String orderInfo, String momoOrderId);
    
    /**
     * Process MoMo payment callback (IPN)
     * 
     * @param callback Callback data from MoMo
     * @return true if processed successfully
     */
    boolean processMomoCallback(PaymentCallbackRequest callback);
    
    /**
     * Get payment transaction by request ID
     * 
     * @param requestId Request ID
     * @return PaymentTransaction
     */
    PaymentTransaction getPaymentByRequestId(String requestId);
    
    /**
     * Get payment transaction by order ID
     * 
     * @param orderId Order ID
     * @return PaymentTransaction (latest one if multiple)
     */
    PaymentTransaction getPaymentByOrderId(Long orderId);
    
    /**
     * Check if order has successful payment
     * 
     * @param orderId Order ID
     * @return true if order has successful payment
     */
    boolean hasSuccessfulPayment(Long orderId);
    
    /**
     * Update order payment status based on payment transaction
     * 
     * @param order Order to update
     * @param transaction Payment transaction
     */
    void updateOrderPaymentStatus(Order order, PaymentTransaction transaction);
}
