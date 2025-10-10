package com.ecommerce.sportcommerce.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating password strength
 */
public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*(),.?\":{}|<>].*");
    
    /**
     * Validate password meets minimum requirements:
     * - At least 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 digit
     * 
     * @param password the password to validate
     * @return true if password is valid
     */
    public static boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        
        return UPPERCASE_PATTERN.matcher(password).matches()
                && LOWERCASE_PATTERN.matcher(password).matches()
                && DIGIT_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validate password with strict requirements (including special character)
     * 
     * @param password the password to validate
     * @return true if password meets strict requirements
     */
    public static boolean isStrongPassword(String password) {
        return isValid(password) && SPECIAL_CHAR_PATTERN.matcher(password).matches();
    }
    
    /**
     * Get validation error message for password
     * 
     * @param password the password to validate
     * @return error message or null if valid
     */
    public static String getValidationError(String password) {
        if (password == null || password.isEmpty()) {
            return "Mật khẩu không được để trống";
        }
        
        if (password.length() < MIN_LENGTH) {
            return "Mật khẩu phải có ít nhất " + MIN_LENGTH + " ký tự";
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            return "Mật khẩu phải có ít nhất 1 chữ hoa";
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            return "Mật khẩu phải có ít nhất 1 chữ thường";
        }
        
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            return "Mật khẩu phải có ít nhất 1 số";
        }
        
        return null;
    }
}
