package com.PBL6.Ecommerce.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for FCM token registration/update from mobile
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFcmTokenDTO {
    
    private String fcmToken;
    private String deviceId;
    private String deviceType; // android, ios, web
}
