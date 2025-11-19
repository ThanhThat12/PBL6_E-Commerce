package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "voucher_products")
public class VoucherProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Vouchers voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vouchers getVoucher() { return voucher; }
    public void setVoucher(Vouchers voucher) { this.voucher = voucher; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}