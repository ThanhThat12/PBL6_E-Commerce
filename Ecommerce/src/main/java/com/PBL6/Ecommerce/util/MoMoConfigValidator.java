package com.PBL6.Ecommerce.util;

import com.PBL6.Ecommerce.config.MoMoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Utility to validate MoMo configuration on startup
 */
@Component
public class MoMoConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoConfigValidator.class);
    
    private final MoMoConfig momoConfig;
    
    public MoMoConfigValidator(MoMoConfig momoConfig) {
        this.momoConfig = momoConfig;
    }
    
    @PostConstruct
    public void validateConfiguration() {
        logger.info("🔍 Validating MoMo Configuration...");
        
        boolean isValid = true;
        
        // Check Partner Code
        if (momoConfig.getPartnerCode() == null || momoConfig.getPartnerCode().isEmpty()) {
            logger.error("❌ MoMo Partner Code is missing!");
            isValid = false;
        } else {
            logger.info("✅ Partner Code: {}", momoConfig.getPartnerCode());
        }
        
        // Check Access Key
        if (momoConfig.getAccessKey() == null || momoConfig.getAccessKey().isEmpty()) {
            logger.error("❌ MoMo Access Key is missing!");
            isValid = false;
        } else {
            logger.info("✅ Access Key: {}***", momoConfig.getAccessKey().substring(0, 4));
        }
        
        // Check Secret Key
        if (momoConfig.getSecretKey() == null || momoConfig.getSecretKey().isEmpty()) {
            logger.error("❌ MoMo Secret Key is missing!");
            isValid = false;
        } else {
            logger.info("✅ Secret Key: {}***", momoConfig.getSecretKey().substring(0, 4));
        }
        
        // Check Endpoint
        if (momoConfig.getEndpoint() == null || momoConfig.getEndpoint().isEmpty()) {
            logger.error("❌ MoMo Endpoint is missing!");
            isValid = false;
        } else {
            logger.info("✅ Endpoint: {}", momoConfig.getEndpoint());
            
            // Validate endpoint format
            if (!momoConfig.getEndpoint().startsWith("https://")) {
                logger.warn("⚠️  MoMo Endpoint should use HTTPS!");
            }
        }
        
        // Check Redirect URL
        if (momoConfig.getRedirectUrl() == null || momoConfig.getRedirectUrl().isEmpty()) {
            logger.error("❌ MoMo Redirect URL is missing!");
            isValid = false;
        } else {
            logger.info("✅ Redirect URL: {}", momoConfig.getRedirectUrl());
        }
        
        // Check IPN URL
        if (momoConfig.getIpnUrl() == null || momoConfig.getIpnUrl().isEmpty()) {
            logger.error("❌ MoMo IPN URL is missing!");
            isValid = false;
        } else {
            logger.info("✅ IPN URL: {}", momoConfig.getIpnUrl());
            
            // Validate IPN URL format
            if (!momoConfig.getIpnUrl().startsWith("https://")) {
                logger.warn("⚠️  MoMo IPN URL should use HTTPS (ngrok required)!");
            }
            
            // Check if using ngrok
            if (momoConfig.getIpnUrl().contains(".ngrok-free.dev") || 
                momoConfig.getIpnUrl().contains(".ngrok.io") ||
                momoConfig.getIpnUrl().contains(".ngrok-free.app")) {
                logger.info("🌐 Using Ngrok tunnel for IPN callback");
                logger.warn("⚠️  Make sure Ngrok is running: ngrok http https://localhost:8081");
            }
        }
        
        // Check Wallet IPN URL
        if (momoConfig.getWalletIpnUrl() == null || momoConfig.getWalletIpnUrl().isEmpty()) {
            logger.warn("⚠️  MoMo Wallet IPN URL is missing!");
        } else {
            logger.info("✅ Wallet IPN URL: {}", momoConfig.getWalletIpnUrl());
        }
        
        // Check Request Type
        if (momoConfig.getRequestType() == null || momoConfig.getRequestType().isEmpty()) {
            logger.error("❌ MoMo Request Type is missing!");
            isValid = false;
        } else {
            logger.info("✅ Request Type: {}", momoConfig.getRequestType());
            
            // Validate request type
            if (!momoConfig.getRequestType().equals("payWithMethod") && 
                !momoConfig.getRequestType().equals("captureWallet")) {
                logger.warn("⚠️  Invalid Request Type. Should be 'payWithMethod' or 'captureWallet'");
            }
        }
        
        // Check Payment Timeout
        if (momoConfig.getPaymentTimeout() == null || momoConfig.getPaymentTimeout() <= 0) {
            logger.warn("⚠️  MoMo Payment Timeout not set, using default: 15 minutes");
        } else {
            logger.info("✅ Payment Timeout: {} minutes", momoConfig.getPaymentTimeout());
        }
        
        // Final validation result
        if (isValid) {
            logger.info("✅ MoMo Configuration is valid and ready!");
        } else {
            logger.error("❌ MoMo Configuration has errors. Please fix before using MoMo payment!");
        }
        
        logger.info("=".repeat(80));
    }
    
    /**
     * Test MoMo configuration by sending a test request
     * Call this method manually when needed
     */
    public void testConnection() {
        logger.info("🧪 Testing MoMo API connection...");
        
        try {
            // Simple test - just check if endpoint is reachable
            java.net.URL url = new java.net.URL(momoConfig.getEndpoint());
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200 || responseCode == 405) { // 405 is OK (POST only)
                logger.info("✅ MoMo API endpoint is reachable");
            } else {
                logger.warn("⚠️  MoMo API returned unexpected status: {}", responseCode);
            }
            
        } catch (Exception e) {
            logger.error("❌ Cannot reach MoMo API endpoint: {}", e.getMessage());
            logger.error("Please check your internet connection");
        }
    }
}
