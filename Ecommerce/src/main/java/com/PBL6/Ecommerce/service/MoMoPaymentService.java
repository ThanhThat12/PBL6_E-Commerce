package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.config.MoMoConfig;
import com.PBL6.Ecommerce.dto.PaymentRequestDTO;
import com.PBL6.Ecommerce.dto.PaymentResponseDTO;
import com.PBL6.Ecommerce.dto.PaymentCallbackRequest;
import com.PBL6.Ecommerce.exception.MoMoPaymentException;
import com.PBL6.Ecommerce.util.MoMoHttpUtil;
import com.PBL6.Ecommerce.util.MoMoSignatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for MoMo payment integration
 */
@Service
public class MoMoPaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoPaymentService.class);
    
    private final MoMoConfig momoConfig;

    public MoMoPaymentService(MoMoConfig momoConfig) {
        this.momoConfig = momoConfig;
    }

    /**
     * Create MoMo payment request and get payment URL
     * 
     * @param orderId Internal order ID
     * @param amount Payment amount
    /**
     * Create MoMo payment request with custom orderId (to avoid duplicate orderId errors)
     * 
     * @param customOrderId Custom unique orderId to send to MoMo (e.g., ORD-123-UUID)
     * @param amount Payment amount
     * @param orderInfo Order information
     * @param requestId Unique request ID
     * @return PaymentResponseDTO containing payment URL
     * @throws MoMoPaymentException if payment creation fails
     */
    public PaymentResponseDTO createPaymentWithCustomOrderId(String customOrderId, BigDecimal amount, 
                                           String orderInfo, String requestId) {
        try {
            logger.info("Creating MoMo payment with custom orderId: {}, amount: {}", customOrderId, amount);
            
            // Convert amount to String (MoMo requires string format for VND)
            // Sử dụng toPlainString() để giữ nguyên số tiền (không mất phần thập phân)
            String amountStr = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
            String extraData = ""; // Can be used for additional data
            
            // Build raw signature string (alphabetical order as per MoMo spec) - sử dụng customOrderId
            String rawSignature = MoMoSignatureUtil.buildRawSignatureForPayment(
                momoConfig.getAccessKey(),
                amountStr,
                extraData,
                momoConfig.getIpnUrl(),
                customOrderId,  // Dùng customOrderId duy nhất thay vì orderId tự tăng
                orderInfo,
                momoConfig.getPartnerCode(),
                momoConfig.getRedirectUrl(),
                requestId,
                momoConfig.getRequestType()
            );
            
            // Generate signature
            String signature = MoMoSignatureUtil.generateSignature(rawSignature, momoConfig.getSecretKey());
            
            logger.debug("Raw signature: {}", rawSignature);
            logger.debug("Generated signature: {}", signature);
            
            // Build request DTO
            PaymentRequestDTO request = new PaymentRequestDTO(
                momoConfig.getPartnerCode(),
                momoConfig.getAccessKey(),
                requestId,
                amountStr,
                customOrderId,  // Gửi customOrderId lên MoMo
                orderInfo,
                momoConfig.getRedirectUrl(),
                momoConfig.getIpnUrl(),
                momoConfig.getRequestType(),
                extraData,
                signature
            );
            
            // Send request to MoMo
            logger.info("Sending payment request to MoMo endpoint with customOrderId: {}: {}", customOrderId, momoConfig.getEndpoint());
            PaymentResponseDTO response = MoMoHttpUtil.sendPostRequest(
                momoConfig.getEndpoint(),
                request,
                PaymentResponseDTO.class
            );
            
            // Check response
            if (response == null) {
                throw new MoMoPaymentException("Received null response from MoMo");
            }
            
            logger.info("MoMo payment response - Result code: {}, Message: {}", 
                       response.getResultCode(), response.getMessage());
            
            if (!response.isSuccess()) {
                throw new MoMoPaymentException(
                    "MoMo payment failed: " + response.getMessage(), 
                    response.getResultCode()
                );
            }
            
            return response;
            
        } catch (MoMoPaymentException e) {
            logger.error("MoMo payment error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create MoMo payment: {}", e.getMessage(), e);
            throw new MoMoPaymentException("Failed to create MoMo payment: " + e.getMessage(), e);
        }
    }

    /**
     * Verify callback signature from MoMo
     * 
     * @param callback Callback request from MoMo
     * @return true if signature is valid
     */
    public boolean verifyCallback(PaymentCallbackRequest callback) {
        try {
            logger.info("Verifying MoMo callback for order: {}, transId: {}", 
                       callback.getOrderId(), callback.getTransId());
            
            // Build raw signature for callback
            String rawSignature = MoMoSignatureUtil.buildRawSignatureForCallback(
                momoConfig.getAccessKey(),
                String.valueOf(callback.getAmount()),
                callback.getExtraData() != null ? callback.getExtraData() : "",
                callback.getMessage(),
                callback.getOrderId(),
                callback.getOrderInfo(),
                callback.getOrderType(),
                callback.getPartnerCode(),
                callback.getPayType(),
                callback.getRequestId(),
                String.valueOf(callback.getResponseTime()),
                String.valueOf(callback.getResultCode()),
                String.valueOf(callback.getTransId())
            );
            
            // Verify signature
            boolean isValid = MoMoSignatureUtil.verifySignature(
                callback.getSignature(), 
                rawSignature, 
                momoConfig.getSecretKey()
            );
            
            if (!isValid) {
                logger.warn("Invalid callback signature for order: {}", callback.getOrderId());
            } else {
                logger.info("Callback signature verified successfully for order: {}", callback.getOrderId());
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error verifying callback signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if callback indicates successful payment
     * 
     * @param callback Callback request from MoMo
     * @return true if payment was successful
     */
    public boolean isPaymentSuccessful(PaymentCallbackRequest callback) {
        return callback != null && callback.isSuccess();
    }

    /**
     * Generate unique request ID
     * 
     * @param prefix Optional prefix
     * @return Unique request ID
     */
    public String generateRequestId(String prefix) {
        return MoMoSignatureUtil.generateRequestId(prefix);
    }

    /**
     * Generate unique request ID without prefix
     * 
     * @return Unique request ID
     */
    public String generateRequestId() {
        return MoMoSignatureUtil.generateRequestId();
    }
}
