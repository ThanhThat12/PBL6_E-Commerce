package com.PBL6.Ecommerce.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;

@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chủ shop
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_address_id")
    private Address pickupAddress;

    // GHN mapping: optional service id and service type id to use when creating shipments for this shop
    @Column(name = "ghn_service_id")
    private Integer ghnServiceId;

    @Column(name = "ghn_service_type_id")
    private Integer ghnServiceTypeId;
    
    // External GHN shop identifier (shop id assigned by GHN). Stored as string to be safe.
    @Column(name = "ghn_shop_id", length = 100)
    private String ghnShopId;
    
    @Column(name = "ghn_token", length = 500)
    private String ghnToken;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShopStatus status = ShopStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Quan hệ 1 shop - nhiều product
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    // Enum trạng thái shop
    public enum ShopStatus {
        ACTIVE,
        INACTIVE
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getPickupAddress() {
        return pickupAddress;
    }
    public void setPickupAddress(Address pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public Integer getGhnServiceId() {
        return ghnServiceId;
    }

    public void setGhnServiceId(Integer ghnServiceId) {
        this.ghnServiceId = ghnServiceId;
    }

    public Integer getGhnServiceTypeId() {
        return ghnServiceTypeId;
    }

    public void setGhnServiceTypeId(Integer ghnServiceTypeId) {
        this.ghnServiceTypeId = ghnServiceTypeId;
    }

    public String getGhnShopId() {
        return ghnShopId;
    }

    public void setGhnShopId(String ghnShopId) {
        this.ghnShopId = ghnShopId;
    }

    public String getGhnToken() {
        return ghnToken;
    }

    public void setGhnToken(String ghnToken) {
        this.ghnToken = ghnToken;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ShopStatus getStatus() {
        return status;
    }

    public void setStatus(ShopStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
    
}
