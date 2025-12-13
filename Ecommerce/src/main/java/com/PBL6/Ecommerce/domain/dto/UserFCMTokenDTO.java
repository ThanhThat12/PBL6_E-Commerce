package com.PBL6.Ecommerce.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFCMTokenDTO {
    private Long userId;
    private String fcmToken;
    private String deviceType;
    private String deviceId;
}
