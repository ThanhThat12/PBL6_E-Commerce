package com.PBL6.Ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.ShopRegistrationDTO;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import java.time.LocalDateTime;

@Service
public class ShopService {
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public Shop registerShop(Long userId, ShopRegistrationDTO shopRegistrationDTO) {
        // Tìm user theo ID
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        // Kiểm tra xem user đã có shop chưa
        if (shopRepository.existsByOwner(user)) {
            throw new RuntimeException("Người dùng đã có shop");
        }
        
        // Kiểm tra tên shop đã tồn tại chưa
        if (shopRepository.existsByName(shopRegistrationDTO.getName())) {
            throw new RuntimeException("Tên shop đã tồn tại");
        }
        
        // Tạo shop mới
        Shop shop = new Shop();
        shop.setOwner(user);
        shop.setName(shopRegistrationDTO.getName());
        shop.setAddress(shopRegistrationDTO.getAddress());
        shop.setDescription(shopRegistrationDTO.getDescription());
        shop.setStatus(Shop.ShopStatus.ACTIVE);
        shop.setCreatedAt(LocalDateTime.now());
        
        return shopRepository.save(shop);
    }
    
    public Shop getShopByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return shopRepository.findByOwner(user).orElse(null);
    }
    
    public boolean hasShop(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return shopRepository.existsByOwner(user);
    }
}