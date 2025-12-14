package com.PBL6.Ecommerce.domain.entity.user;

import java.time.LocalDateTime;

import com.PBL6.Ecommerce.constant.TypeAddress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Type of address:
     * - HOME: Buyer's delivery address (can have multiple, one primary)
     * - STORE: Seller's warehouse/shop address (only one per seller, no primary)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_address", length = 50, nullable = false)
    private TypeAddress typeAddress = TypeAddress.HOME; // Default to HOME

    @Column(length = 500)
    private String fullAddress;

    private Integer provinceId;
    private Integer districtId;
    private String wardCode;

    // Store human-readable names for display
    @Column(length = 100)
    private String provinceName;
    
    @Column(length = 100)
    private String districtName;
    
    @Column(length = 100)
    private String wardName;

    @Column(length = 30)
    private String contactPhone;

    @Column(length = 100)
    private String contactName;

    /**
     * Primary address flag - CHỈ áp dụng cho type_address = HOME
     * Business Rules:
     * - Mỗi user chỉ có tối đa 1 địa chỉ HOME được đánh dấu primary
     * - STORE address luôn có primaryAddress = false
     * - Dùng để chọn địa chỉ mặc định khi checkout
     */
    @Column(nullable = false)
    private boolean primaryAddress = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public TypeAddress getTypeAddress() { return typeAddress; }
    public void setTypeAddress(TypeAddress typeAddress) { this.typeAddress = typeAddress; }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }

    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }

    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }

    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public boolean isPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(boolean primaryAddress) { this.primaryAddress = primaryAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @jakarta.persistence.PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * PostLoad hook to handle null type_address from database
     * Set default to HOME if null to prevent enum conversion errors
     */
    @jakarta.persistence.PostLoad
    public void handleNullTypeAddress() {
        if (this.typeAddress == null) {
            this.typeAddress = TypeAddress.HOME;
        }
    }
}