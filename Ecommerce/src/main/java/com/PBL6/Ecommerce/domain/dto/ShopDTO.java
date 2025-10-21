package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;
import com.PBL6.Ecommerce.domain.Shop;
public class ShopDTO {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private String name;
    private String address;
    private String description;
    private String status;
    private boolean verified;
    private LocalDateTime createdAt;

    public ShopDTO() {}

    public ShopDTO(Long id, Long ownerId, String ownerName, String name, String address, String description, String status, LocalDateTime createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.name = name;
        this.address = address;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ShopDTO fromEntity(Shop shop) {
        if (shop == null) return null;
        ShopDTO dto = new ShopDTO();
        dto.setId(shop.getId());
        if (shop.getOwner() != null) {
            dto.setOwnerId(shop.getOwner().getId());
            dto.setOwnerName(shop.getOwner().getUsername()); // Sửa ở đây
        }
        dto.setName(shop.getName());
        dto.setAddress(shop.getAddress());
        dto.setDescription(shop.getDescription());
        dto.setStatus(shop.getStatus() != null ? shop.getStatus().name() : null);
        dto.setVerified(shop.isVerified());
        dto.setCreatedAt(shop.getCreatedAt());
        return dto;
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}