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
     * Create MoMo payment with custom IPN URL (for wallet deposits)
     * 
     * @param customOrderId Custom unique orderId
     * @param amount Payment amount
     * @param orderInfo Order information
     * @param requestId Unique request ID
     * @param customIpnUrl Custom IPN callback URL
     * @return PaymentResponseDTO containing payment URL
     */
    public PaymentResponseDTO createPaymentWithCustomIpn(String customOrderId, BigDecimal amount, 
                                           String orderInfo, String requestId, String customIpnUrl) {
        try {
            logger.info("Creating MoMo payment with custom IPN - orderId: {}, ipnUrl: {}", customOrderId, customIpnUrl);
            
            String amountStr = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
            String extraData = "";
            
            // Build raw signature with custom IPN URL
            String rawSignature = MoMoSignatureUtil.buildRawSignatureForPayment(
                momoConfig.getAccessKey(),
                amountStr,
                extraData,
                customIpnUrl,  // Use custom IPN URL
                customOrderId,
                orderInfo,
                momoConfig.getPartnerCode(),
                momoConfig.getRedirectUrl(),
                requestId,
                momoConfig.getRequestType()
            );
            
            String signature = MoMoSignatureUtil.generateSignature(rawSignature, momoConfig.getSecretKey());
            
            logger.debug("Custom IPN - Raw signature: {}", rawSignature);
            logger.debug("Custom IPN - Generated signature: {}", signature);
            
            // Build request with custom IPN
            PaymentRequestDTO request = new PaymentRequestDTO(
                momoConfig.getPartnerCode(),
                momoConfig.getAccessKey(),
                requestId,
                amountStr,
                customOrderId,
                orderInfo,
                momoConfig.getRedirectUrl(),
                customIpnUrl,  // Use custom IPN URL
                momoConfig.getRequestType(),
                extraData,
                signature
            );
            
            // Send request to MoMo
            logger.info("Sending payment request to MoMo with custom IPN: {}", momoConfig.getEndpoint());
            PaymentResponseDTO response = MoMoHttpUtil.sendPostRequest(
                momoConfig.getEndpoint(),
                request,
                PaymentResponseDTO.class
            );
            
            if (response == null) {
                throw new MoMoPaymentException("Received null response from MoMo");
            }
            
            logger.info("MoMo payment response (custom IPN) - Result code: {}, Message: {}", 
                       response.getResultCode(), response.getMessage());
            
            if (!response.isSuccess()) {
                throw new MoMoPaymentException(
                    "MoMo payment failed: " + response.getMessage(), 
                    response.getResultCode()
                );
            }
            
            return response;
            
        } catch (MoMoPaymentException e) {
            logger.error("MoMo payment error (custom IPN): {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create MoMo payment (custom IPN): {}", e.getMessage(), e);
            throw new MoMoPaymentException("Failed to create MoMo payment: " + e.getMessage(), e);
        }
    }

    /**
     * Create MoMo payment with custom redirect and IPN URLs (for mobile wallet deposits)
     * 
     * @param customOrderId Custom unique orderId
     * @param amount Payment amount
     * @param orderInfo Order information
     * @param requestId Unique request ID
     * @param customRedirectUrl Custom redirect URL (deep link)
     * @param customIpnUrl Custom IPN callback URL
     * @return PaymentResponseDTO containing payment URL
     */
    public PaymentResponseDTO createPaymentWithCustomUrls(String customOrderId, BigDecimal amount, 
                                           String orderInfo, String requestId, 
                                           String customRedirectUrl, String customIpnUrl) {
        try {
            logger.info("Creating MoMo payment with custom URLs - orderId: {}, redirect: {}, ipn: {}", 
                       customOrderId, customRedirectUrl, customIpnUrl);
            
            String amountStr = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
            String extraData = "";
            
            // Build raw signature with custom redirect and IPN URLs
            String rawSignature = MoMoSignatureUtil.buildRawSignatureForPayment(
                momoConfig.getAccessKey(),
                amountStr,
                extraData,
                customIpnUrl,
                customOrderId,
                orderInfo,
                momoConfig.getPartnerCode(),
                customRedirectUrl,  // Use custom redirect URL
                requestId,
                momoConfig.getRequestType()
            );
            
            String signature = MoMoSignatureUtil.generateSignature(rawSignature, momoConfig.getSecretKey());
            
            logger.debug("Custom URLs - Raw signature: {}", rawSignature);
            logger.debug("Custom URLs - Generated signature: {}", signature);
            
            // Build request with custom redirect and IPN
            PaymentRequestDTO request = new PaymentRequestDTO(
                momoConfig.getPartnerCode(),
                momoConfig.getAccessKey(),
                requestId,
                amountStr,
                customOrderId,
                orderInfo,
                customRedirectUrl,  // Use custom redirect URL
                customIpnUrl,
                momoConfig.getRequestType(),
                extraData,
                signature
            );
            
            // Send request to MoMo
            logger.info("Sending payment request to MoMo with custom URLs: {}", momoConfig.getEndpoint());
            PaymentResponseDTO response = MoMoHttpUtil.sendPostRequest(
                momoConfig.getEndpoint(),
                request,
                PaymentResponseDTO.class
            );
            
            if (response == null) {
                throw new MoMoPaymentException("Received null response from MoMo");
            }
            
            logger.info("MoMo payment response (custom URLs) - Result code: {}, Message: {}", 
                       response.getResultCode(), response.getMessage());
            
            if (!response.isSuccess()) {
                throw new MoMoPaymentException(
                    "MoMo payment failed: " + response.getMessage(), 
                    response.getResultCode()
                );
            }
            
            return response;
            
        } catch (MoMoPaymentException e) {
            logger.error("MoMo payment error (custom URLs): {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create MoMo payment (custom URLs): {}", e.getMessage(), e);
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

    /**
     * Refund a MoMo payment
     * 
     * @param orderId Original MoMo orderId (customOrderId)
     * @param amount Refund amount
     * @param transId Original transaction ID from MoMo callback
     * @return true if refund request was successful
     * @throws MoMoPaymentException if refund fails
     */
    public boolean refundMomoPayment(String orderId, BigDecimal amount, String transId) {
        try {
            logger.info("Creating MoMo refund request - orderId: {}, amount: {}, transId: {}", 
                       orderId, amount, transId);
            
            // Convert amount to String (MoMo requires string format for VND)
            String amountStr = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
            String requestId = generateRequestId("REFUND-");
            String description = "Refund for order " + orderId;
            
            // Build raw signature string for refund (alphabetical order)
            String rawSignature = MoMoSignatureUtil.buildRawSignatureForRefund(
                momoConfig.getAccessKey(),
                amountStr,
                description,
                orderId,
                momoConfig.getPartnerCode(),
                requestId,
                String.valueOf(transId)
            );
            
            // Generate signature
            String signature = MoMoSignatureUtil.generateSignature(rawSignature, momoConfig.getSecretKey());
            
            logger.debug("Refund raw signature: {}", rawSignature);
            logger.debug("Refund signature: {}", signature);
            
            // Build refund request (MoMo refund endpoint)
            // Note: Adjust endpoint if needed - typically /pay or /refund endpoint
            String refundEndpoint = momoConfig.getEndpoint().replace("/pay", "/refund");
            if (refundEndpoint.equals(momoConfig.getEndpoint())) {
                // If no /pay in endpoint, append /refund
                refundEndpoint = momoConfig.getEndpoint() + "/refund";
            }
            
            // Create refund request map
            java.util.Map<String, String> refundRequest = new java.util.HashMap<>();
            refundRequest.put("partnerCode", momoConfig.getPartnerCode());
            refundRequest.put("accessKey", momoConfig.getAccessKey());
            refundRequest.put("requestId", requestId);
            refundRequest.put("amount", amountStr);
            refundRequest.put("orderId", orderId);
            refundRequest.put("transId", transId);
            refundRequest.put("description", description);
            refundRequest.put("signature", signature);
            
            // Send refund request to MoMo
            logger.info("Sending refund request to MoMo endpoint: {}", refundEndpoint);
            PaymentResponseDTO response = MoMoHttpUtil.sendPostRequest(
                refundEndpoint,
                refundRequest,
                PaymentResponseDTO.class
            );
            
            if (response == null) {
                logger.warn("Received null response from MoMo refund request");
                throw new MoMoPaymentException("Received null response from MoMo refund");
            }
            
            logger.info("MoMo refund response - Result code: {}, Message: {}", 
                       response.getResultCode(), response.getMessage());
            
            if (!response.isSuccess()) {
                logger.warn("MoMo refund failed: {} - {}", response.getResultCode(), response.getMessage());
                throw new MoMoPaymentException(
                    "MoMo refund failed: " + response.getMessage(), 
                    response.getResultCode()
                );
            }
            
            logger.info("MoMo refund successful for orderId: {}, amount: {}", orderId, amount);
            return true;
            
        } catch (MoMoPaymentException e) {
            logger.error("MoMo refund error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to process MoMo refund: {}", e.getMessage(), e);
            throw new MoMoPaymentException("Failed to process MoMo refund: " + e.getMessage(), e);
        }
    }

    /**
     * Refund MoMo payment with detailed response
     * 
     * @param orderId MoMo order ID
     * @param requestId Unique refund request ID
     * @param amount Refund amount
     * @param transId MoMo transaction ID from original payment
     * @return MoMoRefundResponseDTO containing refund details
     */
    public com.PBL6.Ecommerce.dto.MoMoRefundResponseDTO refundPayment(String orderId, String requestId, 
                                                                      BigDecimal amount, String transId) {
        try {
            logger.info("Processing MoMo refund - orderId: {}, amount: {}, transId: {}", orderId, amount, transId);
            
            // Convert amount to String
            String amountStr = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
            String description = "Refund for order " + orderId;
            
            // Build raw signature for refund
            String rawSignature = MoMoSignatureUtil.buildRawSignatureForRefund(
                momoConfig.getAccessKey(),
                amountStr,
                description,
                orderId,
                momoConfig.getPartnerCode(),
                requestId,
                transId
            );
            
            // Generate signature
            String signature = MoMoSignatureUtil.generateSignature(rawSignature, momoConfig.getSecretKey());
            
            // Build refund endpoint
            String refundEndpoint = momoConfig.getEndpoint().replace("/create", "/refund");
            
            // Create refund request
            java.util.Map<String, String> refundRequest = new java.util.HashMap<>();
            refundRequest.put("partnerCode", momoConfig.getPartnerCode());
            refundRequest.put("accessKey", momoConfig.getAccessKey());
            refundRequest.put("requestId", requestId);
            refundRequest.put("amount", amountStr);
            refundRequest.put("orderId", orderId);
            refundRequest.put("transId", transId);
            refundRequest.put("description", description);
            refundRequest.put("signature", signature);
            
            // Send request to MoMo
            logger.info("Sending refund request to: {}", refundEndpoint);
            com.PBL6.Ecommerce.dto.MoMoRefundResponseDTO response = MoMoHttpUtil.sendPostRequest(
                refundEndpoint,
                refundRequest,
                com.PBL6.Ecommerce.dto.MoMoRefundResponseDTO.class
            );
            
            if (response == null) {
                throw new MoMoPaymentException("Received null response from MoMo refund");
            }
            
            logger.info("MoMo refund response - Result code: {}, Message: {}", 
                       response.getResultCode(), response.getMessage());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to process MoMo refund: {}", e.getMessage(), e);
            throw new MoMoPaymentException("Failed to process MoMo refund: " + e.getMessage(), e);
        }
    }
}
