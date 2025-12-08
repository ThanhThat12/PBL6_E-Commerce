package com.PBL6.Ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MoMoConfig {
    
    @Value("${momo.partnerCode}")
    private String partnerCode;
    
    @Value("${momo.accessKey}")
    private String accessKey;
    
    @Value("${momo.secretKey}")
    private String secretKey;
    
    @Value("${momo.endpoint}")
    private String endpoint;
    
    @Value("${momo.redirectUrl}")
    private String redirectUrl;
    
    @Value("${momo.mobile.redirectUrl}")
    private String mobileRedirectUrl;
    
    @Value("${momo.ipnUrl}")
    private String ipnUrl;
    
    @Value("${momo.wallet.ipnUrl}")
    private String walletIpnUrl;
    
    @Value("${momo.requestType:payWithMethod}")
    private String requestType;
    
    @Value("${momo.paymentTimeout:15}")
    private Integer paymentTimeout;

    // Getters
    public String getPartnerCode() {
        return partnerCode;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public String getMobileRedirectUrl() {
        return mobileRedirectUrl;
    }
    
    public String getIpnUrl() {
        return ipnUrl;
    }    public String getWalletIpnUrl() {
        return walletIpnUrl;
    }

    public String getRequestType() {
        return requestType;
    }

    public Integer getPaymentTimeout() {
        return paymentTimeout;
    }

    @Override
    public String toString() {
        return "MoMoConfig{" +
                "partnerCode='" + partnerCode + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", redirectUrl='" + redirectUrl + '\'' +
                ", ipnUrl='" + ipnUrl + '\'' +
                ", requestType='" + requestType + '\'' +
                ", paymentTimeout=" + paymentTimeout +
                '}';
    }
}
