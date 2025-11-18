package com.PBL6.Ecommerce.domain.dto.admin;

public class ListSellerUserDTO {
    private Long id;
    private String shopName;
    private String phoneNumber;
    private String email;
    private Integer totalProducts;
    private String status;
    private Double revenue;

    public ListSellerUserDTO(Long id, String shopName, String phoneNumber, String email,
                            Integer totalProducts, String status, Double revenue) {
        this.id = id;
        this.shopName = shopName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.totalProducts = totalProducts;
        this.status = status;
        this.revenue = revenue;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }
}