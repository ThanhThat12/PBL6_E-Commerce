package com.PBL6.Ecommerce.controller.notification;

import com.PBL6.Ecommerce.domain.dto.NotificationDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.entity.notification.Notification;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.repository.NotificationRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Notifications", description = "API quản lý thông báo")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách thông báo của user hiện tại
     */
    @Operation(summary = "Lấy danh sách thông báo")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<List<NotificationDTO>>> getNotifications(
            @AuthenticationPrincipal Jwt jwt) {
        
        String email = jwt.getClaimAsString("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

        List<NotificationDTO> dtos = notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ResponseDTO<>(200, "Lấy danh sách thông báo thành công", null, dtos));
    }

    /**
     * Đếm số lượng thông báo chưa đọc
     */
    @Operation(summary = "Đếm thông báo chưa đọc")
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<List<java.util.Map<String, Long>>>> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt) {
        
        String email = jwt.getClaimAsString("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long count = notificationRepository.countUnreadByUserId(user.getId());

        ResponseDTO<List<java.util.Map<String, Long>>> response = 
                new ResponseDTO<>(200, "Lấy số lượng thông báo chưa đọc thành công", null, 
                        List.of(java.util.Map.of("count", count)));
        return ResponseEntity.ok(response);
    }

    /**
     * Đánh dấu một thông báo là đã đọc
     */
    @Operation(summary = "Đánh dấu đã đọc")
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ResponseDTO<List<NotificationDTO>>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String email = jwt.getClaimAsString("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Verify ownership
        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO<>(403, "Không có quyền", "FORBIDDEN", null));
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        NotificationDTO dto = convertToDTO(notification);
        ResponseDTO<List<NotificationDTO>> response = 
                new ResponseDTO<>(200, "Đã đánh dấu là đã đọc", null, List.of(dto));
        return ResponseEntity.ok(response);
    }

    /**
     * Đánh dấu tất cả thông báo là đã đọc
     */
    @Operation(summary = "Đánh dấu tất cả đã đọc")
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ResponseDTO<List<java.util.Map<String, Integer>>>> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt) {
        
        String email = jwt.getClaimAsString("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int count = notificationRepository.markAllAsReadByUserId(user.getId());

        ResponseDTO<List<java.util.Map<String, Integer>>> response = 
                new ResponseDTO<>(200, "Đã đánh dấu " + count + " thông báo là đã đọc", null, 
                        List.of(java.util.Map.of("count", count)));
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa một thông báo
     */
    @Operation(summary = "Xóa thông báo")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ResponseDTO<Void>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String email = jwt.getClaimAsString("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Verify ownership
        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO<>(403, "Không có quyền", "FORBIDDEN", null));
        }

        notificationRepository.delete(notification);

        return ResponseEntity.ok(new ResponseDTO<>(200, "Đã xóa thông báo", null, null));
    }

    /**
     * Xóa tất cả thông báo
     */
    @Operation(summary = "Xóa tất cả thông báo")
    @DeleteMapping("/clear-all")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ResponseDTO<Void>> clearAllNotifications(
            @AuthenticationPrincipal Jwt jwt) {
        
        String email = jwt.getClaimAsString("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationRepository.deleteByUserId(user.getId());

        return ResponseEntity.ok(new ResponseDTO<>(200, "Đã xóa tất cả thông báo", null, null));
    }

    /**
     * Convert entity to DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setOrderId(notification.getOrderId());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
