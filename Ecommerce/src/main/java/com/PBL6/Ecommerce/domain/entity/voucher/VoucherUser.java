package com.PBL6.Ecommerce.domain.entity.voucher;

import com.PBL6.Ecommerce.domain.entity.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "voucher_users")
public class VoucherUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Vouchers voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vouchers getVoucher() { return voucher; }
    public void setVoucher(Vouchers voucher) { this.voucher = voucher; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
