package com.ecommerce.sportcommerce.util;

import java.security.SecureRandom;

/**
 * Utility class for generating OTP codes
 */
public class OtpGenerator {
    
    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    
    /**
     * Generate a 6-digit OTP code
     * @return 6-digit OTP as String
     */
    public static String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    /**
     * Generate OTP with custom length
     * @param length desired OTP length
     * @return OTP code as String
     */
    public static String generateOtp(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be positive");
        }
        
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
