package com.PBL6.Ecommerce.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Service for handling rate limiting on authentication endpoints.
 * Implements rate limiting for:
 * - Login attempts: 5 per 15 minutes per IP
 * - OTP resend requests: 3 per 15 minutes per contact
 * - OTP verification: 5 per 15 minutes per contact
 * - Global requests: 100 per minute per IP
 */
@Service
public class LoginAttemptService {
    
    // Key format: "login:{ip}", Value: List of attempt timestamps
    private final ConcurrentHashMap<String, List<Long>> loginAttempts = new ConcurrentHashMap<>();
    
    // Key format: "otp_resend:{contact}", Value: List of attempt timestamps
    private final ConcurrentHashMap<String, List<Long>> otpResendAttempts = new ConcurrentHashMap<>();
    
    // Key format: "otp_verify:{contact}", Value: List of attempt timestamps
    private final ConcurrentHashMap<String, List<Long>> otpVerifyAttempts = new ConcurrentHashMap<>();
    
    // Key format: "global:{ip}", Value: List of attempt timestamps
    private final ConcurrentHashMap<String, List<Long>> globalAttempts = new ConcurrentHashMap<>();
    
    // Key format: "ip_lockout:{ip}", Value: unlock time (milliseconds)
    private final ConcurrentHashMap<String, Long> ipLockouts = new ConcurrentHashMap<>();
    
    // Key format: "contact_lockout:{contact}", Value: unlock time (milliseconds)
    private final ConcurrentHashMap<String, Long> contactLockouts = new ConcurrentHashMap<>();
    
    private static final long FIFTEEN_MINUTES = 15 * 60 * 1000; // 15 minutes in milliseconds
    private static final long ONE_MINUTE = 60 * 1000; // 1 minute in milliseconds
    private static final long LOCKOUT_DURATION = 15 * 60 * 1000; // 15 minutes lockout
    
    // Rate limiting thresholds
    private static final int LOGIN_ATTEMPTS_LIMIT = 5;
    private static final int OTP_RESEND_LIMIT = 3;
    private static final int OTP_VERIFY_LIMIT = 5;
    private static final int GLOBAL_REQUESTS_LIMIT = 100;
    
    /**
     * Check if login attempt is allowed for the given IP
     * @param ip Client IP address
     * @return true if attempt is allowed, false if rate limit exceeded
     * @throws RuntimeException if IP is locked out
     */
    public boolean isLoginAttemptAllowed(String ip) {
        if (isIpLocked(ip)) {
            throw new RuntimeException("IP bị khóa tạm thời do nhiều lần đăng nhập thất bại. Vui lòng thử lại sau 15 phút.");
        }
        
        String key = "login:" + ip;
        return checkAttempts(loginAttempts, key, LOGIN_ATTEMPTS_LIMIT, FIFTEEN_MINUTES);
    }
    
    /**
     * Record a failed login attempt
     * @param ip Client IP address
     */
    public void recordLoginAttempt(String ip) {
        String key = "login:" + ip;
        recordAttempt(loginAttempts, key);
    }
    
    /**
     * Check if OTP resend is allowed for the given contact
     * @param contact Email or phone number
     * @return true if resend is allowed, false if rate limit exceeded
     * @throws RuntimeException if contact is locked out
     */
    public boolean isOtpResendAllowed(String contact) {
        if (isContactLocked(contact)) {
            throw new RuntimeException("Bạn đã gửi OTP quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }
        
        String key = "otp_resend:" + contact;
        return checkAttempts(otpResendAttempts, key, OTP_RESEND_LIMIT, FIFTEEN_MINUTES);
    }
    
    /**
     * Record an OTP resend attempt
     * @param contact Email or phone number
     */
    public void recordOtpResendAttempt(String contact) {
        String key = "otp_resend:" + contact;
        recordAttempt(otpResendAttempts, key);
    }
    
    /**
     * Check if OTP verification is allowed for the given contact
     * @param contact Email or phone number
     * @return true if verification is allowed, false if rate limit exceeded
     * @throws RuntimeException if contact is locked out
     */
    public boolean isOtpVerifyAllowed(String contact) {
        if (isContactLocked(contact)) {
            throw new RuntimeException("Bạn đã xác thực OTP quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }
        
        String key = "otp_verify:" + contact;
        return checkAttempts(otpVerifyAttempts, key, OTP_VERIFY_LIMIT, FIFTEEN_MINUTES);
    }
    
    /**
     * Record an OTP verification attempt
     * @param contact Email or phone number
     */
    public void recordOtpVerifyAttempt(String contact) {
        String key = "otp_verify:" + contact;
        recordAttempt(otpVerifyAttempts, key);
    }
    
    /**
     * Check if global request is allowed for the given IP
     * @param ip Client IP address
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isGlobalRequestAllowed(String ip) {
        String key = "global:" + ip;
        return checkAttempts(globalAttempts, key, GLOBAL_REQUESTS_LIMIT, ONE_MINUTE);
    }
    
    /**
     * Record a global request attempt
     * @param ip Client IP address
     */
    public void recordGlobalAttempt(String ip) {
        String key = "global:" + ip;
        recordAttempt(globalAttempts, key);
    }
    
    /**
     * Lock an IP address after too many failed login attempts
     * @param ip Client IP address
     */
    public void lockIp(String ip) {
        long unlockTime = System.currentTimeMillis() + LOCKOUT_DURATION;
        ipLockouts.put("ip_lockout:" + ip, unlockTime);
        loginAttempts.remove("login:" + ip); // Clear attempts
    }
    
    /**
     * Lock a contact after too many failed OTP attempts
     * @param contact Email or phone number
     */
    public void lockContact(String contact) {
        long unlockTime = System.currentTimeMillis() + LOCKOUT_DURATION;
        contactLockouts.put("contact_lockout:" + contact, unlockTime);
        otpResendAttempts.remove("otp_resend:" + contact);
        otpVerifyAttempts.remove("otp_verify:" + contact);
    }
    
    /**
     * Check if IP is currently locked
     * @param ip Client IP address
     * @return true if IP is locked, false otherwise
     */
    public boolean isIpLocked(String ip) {
        String key = "ip_lockout:" + ip;
        Long unlockTime = ipLockouts.get(key);
        if (unlockTime == null) {
            return false;
        }
        
        if (System.currentTimeMillis() > unlockTime) {
            ipLockouts.remove(key);
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if contact is currently locked
     * @param contact Email or phone number
     * @return true if contact is locked, false otherwise
     */
    public boolean isContactLocked(String contact) {
        String key = "contact_lockout:" + contact;
        Long unlockTime = contactLockouts.get(key);
        if (unlockTime == null) {
            return false;
        }
        
        if (System.currentTimeMillis() > unlockTime) {
            contactLockouts.remove(key);
            return false;
        }
        
        return true;
    }
    
    /**
     * Clear all attempts for a user after successful login
     * @param ip Client IP address
     */
    public void clearLoginAttempts(String ip) {
        loginAttempts.remove("login:" + ip);
    }
    
    /**
     * Get remaining login attempts for an IP
     * @param ip Client IP address
     * @return Number of remaining attempts
     */
    public int getRemainingLoginAttempts(String ip) {
        String key = "login:" + ip;
        List<Long> attempts = loginAttempts.getOrDefault(key, new ArrayList<>());
        cleanupOldAttempts(attempts, FIFTEEN_MINUTES);
        return Math.max(0, LOGIN_ATTEMPTS_LIMIT - attempts.size());
    }
    
    /**
     * Get remaining OTP verification attempts for a contact
     * @param contact Email or phone number
     * @return Number of remaining attempts
     */
    public int getRemainingOtpVerifyAttempts(String contact) {
        String key = "otp_verify:" + contact;
        List<Long> attempts = otpVerifyAttempts.getOrDefault(key, new ArrayList<>());
        cleanupOldAttempts(attempts, FIFTEEN_MINUTES);
        return Math.max(0, OTP_VERIFY_LIMIT - attempts.size());
    }
    
    /**
     * Reset rate limiting for a contact (used after successful OTP verification)
     * @param contact Email or phone number
     */
    public void resetContactAttempts(String contact) {
        otpResendAttempts.remove("otp_resend:" + contact);
        otpVerifyAttempts.remove("otp_verify:" + contact);
    }
    
    // ============= Private Helper Methods =============
    
    /**
     * Check if an attempt is allowed based on rate limiting rules
     */
    private boolean checkAttempts(ConcurrentHashMap<String, List<Long>> attempts, String key, 
                                 int limit, long timeWindow) {
        List<Long> attemptList = attempts.computeIfAbsent(key, k -> new ArrayList<>());
        cleanupOldAttempts(attemptList, timeWindow);
        return attemptList.size() < limit;
    }
    
    /**
     * Record a new attempt
     */
    private void recordAttempt(ConcurrentHashMap<String, List<Long>> attempts, String key) {
        List<Long> attemptList = attempts.computeIfAbsent(key, k -> new ArrayList<>());
        attemptList.add(System.currentTimeMillis());
    }
    
    /**
     * Remove old attempts that are outside the time window
     */
    private void cleanupOldAttempts(List<Long> attempts, long timeWindow) {
        long cutoffTime = System.currentTimeMillis() - timeWindow;
        attempts.removeIf(timestamp -> timestamp < cutoffTime);
    }
}
