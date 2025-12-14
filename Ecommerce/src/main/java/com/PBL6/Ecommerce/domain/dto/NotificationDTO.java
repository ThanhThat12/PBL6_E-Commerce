package com.PBL6.Ecommerce.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private String message;
    private Long orderId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
