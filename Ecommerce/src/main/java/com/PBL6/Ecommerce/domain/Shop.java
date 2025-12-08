package com.PBL6.Ecommerce.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "shops", indexes = {
    @Index(name = "idx_submitted_at", columnList = "submitted_at"),
    @Index(name = "idx_reviewed_by", columnList = "reviewed_by"),
    @Index(name = "idx_accept_cod", columnList = "accept_cod")
})
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

    // Shop contact info
    @Column(name = "shop_phone", length = 15)
    private String shopPhone;

    @Column(name = "shop_email", length = 100)
    private String shopEmail;

    // External GHN shop identifier (shop id assigned by GHN). Stored as string to be safe.
    @Column(name = "ghn_shop_id", length = 100)
    private String ghnShopId;

    @Column(name = "ghn_token", length = 500)
    private String ghnToken;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShopStatus status = ShopStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Shop branding images (Phase 5: User Story 3)
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "logo_public_id", length = 255)
    private String logoPublicId;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "banner_public_id", length = 255)
    private String bannerPublicId;

    // ========== KYC - Xác thực danh tính ==========
    @Column(name = "id_card_number", length = 20)
    private String idCardNumber;

    @Column(name = "id_card_front_url", length = 500)
    private String idCardFrontUrl;

    @Column(name = "id_card_front_public_id", length = 255)
    private String idCardFrontPublicId;

    @Column(name = "id_card_back_url", length = 500)
    private String idCardBackUrl;

    @Column(name = "id_card_back_public_id", length = 255)
    private String idCardBackPublicId;

    @Column(name = "selfie_with_id_url", length = 500)
    private String selfieWithIdUrl;

    @Column(name = "selfie_with_id_public_id", length = 255)
    private String selfieWithIdPublicId;

    @Column(name = "id_card_name", length = 100)
    private String idCardName;

    // ========== Payment Methods - Phase 1: COD only ==========
    @Column(name = "accept_cod", nullable = false)
    private Boolean acceptCod = true;

    @Column(name = "cod_fee_percentage", precision = 5, scale = 2)
    private BigDecimal codFeePercentage = new BigDecimal("2.00");

    // ========== Review tracking - xét duyệt ==========
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // ========== Shop rating ==========
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("5.00");

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    // Quan hệ 1 shop - nhiều product
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    // Enum trạng thái shop
    public enum ShopStatus {
        PENDING,   // Shop chờ duyệt
        ACTIVE,    // Shop đang hoạt động
        INACTIVE,  // Shop tạm ngưng
        REJECTED,  // Shop bị từ chối đăng ký
        SUSPENDED, // Shop bị đình chỉ (vi phạm)
        CLOSED     // Shop đóng cửa vĩnh viễn
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

    // Getters and setters for shop branding images
    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLogoPublicId() {
        return logoPublicId;
    }

    public void setLogoPublicId(String logoPublicId) {
        this.logoPublicId = logoPublicId;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getBannerPublicId() {
        return bannerPublicId;
    }

    public void setBannerPublicId(String bannerPublicId) {
        this.bannerPublicId = bannerPublicId;
    }

    // Getters and setters for shop contact
    public String getShopPhone() {
        return shopPhone;
    }

    public void setShopPhone(String shopPhone) {
        this.shopPhone = shopPhone;
    }

    public String getShopEmail() {
        return shopEmail;
    }

    public void setShopEmail(String shopEmail) {
        this.shopEmail = shopEmail;
    }

    // Getters and setters for KYC fields
    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getIdCardFrontUrl() {
        return idCardFrontUrl;
    }

    public void setIdCardFrontUrl(String idCardFrontUrl) {
        this.idCardFrontUrl = idCardFrontUrl;
    }

    public String getIdCardFrontPublicId() {
        return idCardFrontPublicId;
    }

    public void setIdCardFrontPublicId(String idCardFrontPublicId) {
        this.idCardFrontPublicId = idCardFrontPublicId;
    }

    public String getIdCardBackUrl() {
        return idCardBackUrl;
    }

    public void setIdCardBackUrl(String idCardBackUrl) {
        this.idCardBackUrl = idCardBackUrl;
    }

    public String getIdCardBackPublicId() {
        return idCardBackPublicId;
    }

    public void setIdCardBackPublicId(String idCardBackPublicId) {
        this.idCardBackPublicId = idCardBackPublicId;
    }

    public String getSelfieWithIdUrl() {
        return selfieWithIdUrl;
    }

    public void setSelfieWithIdUrl(String selfieWithIdUrl) {
        this.selfieWithIdUrl = selfieWithIdUrl;
    }

    public String getSelfieWithIdPublicId() {
        return selfieWithIdPublicId;
    }

    public void setSelfieWithIdPublicId(String selfieWithIdPublicId) {
        this.selfieWithIdPublicId = selfieWithIdPublicId;
    }

    public String getIdCardName() {
        return idCardName;
    }

    public void setIdCardName(String idCardName) {
        this.idCardName = idCardName;
    }

    // Getters and setters for payment methods
    public Boolean getAcceptCod() {
        return acceptCod;
    }

    public void setAcceptCod(Boolean acceptCod) {
        this.acceptCod = acceptCod;
    }

    public BigDecimal getCodFeePercentage() {
        return codFeePercentage;
    }

    public void setCodFeePercentage(BigDecimal codFeePercentage) {
        this.codFeePercentage = codFeePercentage;
    }

    // Getters and setters for review tracking
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    // Getters and setters for shop rating
    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

}
