package com.ecommerce.sportcommerce.security.oauth2;

import java.util.Map;

import com.ecommerce.sportcommerce.exception.BadRequestException;

/**
 * Factory to create OAuth2UserInfo based on provider
 */
public class OAuth2UserInfoFactory {
    
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("facebook")) {
            return new FacebookOAuth2UserInfo(attributes);
        } else {
            throw new BadRequestException("Đăng nhập với " + registrationId + " không được hỗ trợ");
        }
    }
}
