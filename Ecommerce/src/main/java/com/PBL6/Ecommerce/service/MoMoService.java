package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.PaymentTransaction;
import com.PBL6.Ecommerce.dto.PaymentCallbackRequest;
import com.PBL6.Ecommerce.dto.PaymentResponseDTO;

import java.math.BigDecimal;

public interface MoMoService {
    PaymentResponseDTO createMomoPayment(Long orderId, BigDecimal amount, String orderInfo);
    PaymentResponseDTO createMomoPaymentWithCustomOrderId(Long orderId, BigDecimal amount, String orderInfo, String momoOrderId);
    boolean processMomoCallback(PaymentCallbackRequest callback);
    PaymentTransaction getPaymentByRequestId(String requestId);
    PaymentTransaction getPaymentByOrderId(Long orderId);
    boolean hasSuccessfulPayment(Long orderId);
    void updateOrderPaymentStatus(Order order, PaymentTransaction transaction);
}
