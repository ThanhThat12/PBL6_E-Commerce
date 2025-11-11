package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Refund;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Wallet;
import com.PBL6.Ecommerce.repository.RefundRepository;
import com.PBL6.Ecommerce.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RefundService {
    private final RefundRepository refundRepository;
    private final WalletRepository walletRepository;

    public RefundService(RefundRepository refundRepository, WalletRepository walletRepository) {
        this.refundRepository = refundRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Refund createAndCompleteRefund(Order order, BigDecimal amount, String reason) {
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setStatus(Refund.RefundStatus.COMPLETED);
        refund = refundRepository.save(refund);

        // Hoàn tiền vào ví SportyPay
        User user = order.getUser();
        Wallet wallet = walletRepository.findByUser(user).orElseThrow();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        return refund;
    }
}
