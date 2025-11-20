package com.PBL6.Ecommerce.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for generating MoMo payment signatures
 * According to MoMo API documentation: https://developers.momo.vn
 */
public class MoMoSignatureUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoSignatureUtil.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generate HMAC SHA256 signature for MoMo payment request
     * 
     * @param rawSignature The raw signature string (concatenated parameters)
     * @param secretKey The secret key from MoMo
     * @return The HMAC SHA256 signature in hexadecimal format
     */
    public static String generateSignature(String rawSignature, String secretKey) {
        try {
            logger.debug("Generating signature for raw string: {}", rawSignature);
            
            Mac hmacSHA256 = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            hmacSHA256.init(secretKeySpec);
            
            byte[] hash = hmacSHA256.doFinal(rawSignature.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String signature = hexString.toString();
            logger.debug("Generated signature: {}", signature);
            
            return signature;
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error generating signature: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate MoMo signature", e);
        }
    }

    /**
     * Build raw signature string for payment request
     * Format: accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType
     * 
     * @param accessKey Partner access key
     * @param amount Payment amount
     * @param extraData Extra data (can be empty string)
     * @param ipnUrl IPN callback URL
     * @param orderId Order ID
     * @param orderInfo Order information
     * @param partnerCode Partner code
     * @param redirectUrl Redirect URL after payment
     * @param requestId Unique request ID
     * @param requestType Request type (e.g., "payWithMethod")
     * @return Raw signature string
     */
    public static String buildRawSignatureForPayment(String accessKey, String amount, String extraData,
                                                     String ipnUrl, String orderId, String orderInfo,
                                                     String partnerCode, String redirectUrl,
                                                     String requestId, String requestType) {
        return "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;
    }

    /**
     * Build raw signature string for callback verification
     * Format: accessKey=$accessKey&amount=$amount&extraData=$extraData&message=$message&orderId=$orderId&orderInfo=$orderInfo&orderType=$orderType&partnerCode=$partnerCode&payType=$payType&requestId=$requestId&responseTime=$responseTime&resultCode=$resultCode&transId=$transId
     * 
     * @param accessKey Partner access key
     * @param amount Payment amount
     * @param extraData Extra data
     * @param message Response message
     * @param orderId Order ID
     * @param orderInfo Order information
     * @param orderType Order type
     * @param partnerCode Partner code
     * @param payType Payment type
     * @param requestId Request ID
     * @param responseTime Response timestamp
     * @param resultCode Result code
     * @param transId Transaction ID from MoMo
     * @return Raw signature string
     */
    public static String buildRawSignatureForCallback(String accessKey, String amount, String extraData,
                                                      String message, String orderId, String orderInfo,
                                                      String orderType, String partnerCode, String payType,
                                                      String requestId, String responseTime, String resultCode,
                                                      String transId) {
        return "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&message=" + message +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&orderType=" + orderType +
                "&partnerCode=" + partnerCode +
                "&payType=" + payType +
                "&requestId=" + requestId +
                "&responseTime=" + responseTime +
                "&resultCode=" + resultCode +
                "&transId=" + transId;
    }

    /**
     * Build raw signature string for refund request
     * Format: accessKey=$accessKey&amount=$amount&description=$description&orderId=$orderId&partnerCode=$partnerCode&requestId=$requestId&transId=$transId
     * 
     * @param accessKey Partner access key
     * @param amount Refund amount
     * @param description Refund description
     * @param orderId Order ID
     * @param partnerCode Partner code
     * @param requestId Unique request ID
     * @param transId Original transaction ID
     * @return Raw signature string
     */
    public static String buildRawSignatureForRefund(String accessKey, String amount, String description,
                                                    String orderId, String partnerCode,
                                                    String requestId, String transId) {
        return "accessKey=" + accessKey +
                "&amount=" + amount +
                "&description=" + description +
                "&orderId=" + orderId +
                "&partnerCode=" + partnerCode +
                "&requestId=" + requestId +
                "&transId=" + transId;
    }

    /**
     * Verify signature from MoMo callback
     * 
     * @param receivedSignature The signature received from MoMo
     * @param rawSignature The raw signature string to verify
     * @param secretKey The secret key
     * @return true if signature is valid, false otherwise
     */
    public static boolean verifySignature(String receivedSignature, String rawSignature, String secretKey) {
        try {
            String calculatedSignature = generateSignature(rawSignature, secretKey);
            boolean isValid = calculatedSignature.equals(receivedSignature);
            
            if (!isValid) {
                logger.warn("Signature verification failed. Received: {}, Calculated: {}", 
                           receivedSignature, calculatedSignature);
            } else {
                logger.debug("Signature verification successful");
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate unique request ID for MoMo payment
     * Format: timestamp + random string
     * 
     * @param prefix Optional prefix for request ID
     * @return Unique request ID
     */
    public static String generateRequestId(String prefix) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int)(Math.random() * 10000));
        
        if (prefix != null && !prefix.isEmpty()) {
            return prefix + "_" + timestamp + "_" + random;
        }
        
        return timestamp + "_" + random;
    }

    /**
     * Generate unique request ID without prefix
     * 
     * @return Unique request ID
     */
    public static String generateRequestId() {
        return generateRequestId(null);
    }
}
