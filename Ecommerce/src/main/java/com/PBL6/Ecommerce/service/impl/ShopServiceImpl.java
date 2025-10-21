package com.PBL6.Ecommerce.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.ShopService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public ShopServiceImpl(ShopRepository shopRepository, UserRepository userRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ShopDTO createShop(ShopDTO shopDTO) {
        Shop shop = toEntity(shopDTO);
        // Luôn set status PENDING khi tạo mới
        shop.setStatus(Shop.ShopStatus.PENDING);
        // resolve owner
        if (shopDTO.getOwnerId() != null) {
            Optional<User> ownerOpt = userRepository.findById(shopDTO.getOwnerId());
            ownerOpt.ifPresent(shop::setOwner);
        }
        Shop saved = shopRepository.save(shop);
        return toDTO(saved);
    }

    @Override
    public ShopDTO getShopById(Long id) {
        return shopRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    public List<ShopDTO> getShopsByOwnerId(Long ownerId) {
        return shopRepository.findByOwnerId(ownerId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ShopDTO updateShop(Long id, ShopDTO shopDTO) {
        Optional<Shop> opt = shopRepository.findById(id);
        if (!opt.isPresent()) return null;
        Shop shop = opt.get();
        if (shopDTO.getName() != null) shop.setName(shopDTO.getName());
        if (shopDTO.getAddress() != null) shop.setAddress(shopDTO.getAddress());
        if (shopDTO.getDescription() != null) shop.setDescription(shopDTO.getDescription());
        if (shopDTO.getStatus() != null) {
            try {
                shop.setStatus(Shop.ShopStatus.valueOf(shopDTO.getStatus()));
            } catch (IllegalArgumentException e) {
                // ignore invalid status
            }
        }
        // owner change (optional)
        if (shopDTO.getOwnerId() != null) {
            userRepository.findById(shopDTO.getOwnerId()).ifPresent(shop::setOwner);
        }
        Shop saved = shopRepository.save(shop);
        return toDTO(saved);
    }

    @Override
    public void deleteShop(Long id) {
        shopRepository.deleteById(id);
    }

    @Override
    public List<ShopDTO> listAllShops() {
        return shopRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ShopDTO approveShop(Long id) {
        Optional<Shop> opt = shopRepository.findById(id);
        if (!opt.isPresent()) return null;
        Shop shop = opt.get();
        shop.setStatus(Shop.ShopStatus.APPROVED);
        Shop saved = shopRepository.save(shop);
        return toDTO(saved);
    }

    @Override
    public ShopDTO rejectShop(Long id) {
        Optional<Shop> opt = shopRepository.findById(id);
        if (!opt.isPresent()) return null;
        Shop shop = opt.get();
        shop.setStatus(Shop.ShopStatus.REJECTED);
        Shop saved = shopRepository.save(shop);
        return toDTO(saved);
    }

    @Override
    public ShopDTO suspendShop(Long id) {
        Optional<Shop> opt = shopRepository.findById(id);
        if (!opt.isPresent()) return null;
        Shop shop = opt.get();
        shop.setStatus(Shop.ShopStatus.SUSPENDED);
        Shop saved = shopRepository.save(shop);
        return toDTO(saved);
    }

    @Override
    public ShopDTO verifyShop(Long id) {
    Optional<Shop> opt = shopRepository.findById(id);
    if (!opt.isPresent()) return null;
    Shop shop = opt.get();
    shop.setVerified(true);
    // Nếu shop đang bị SUSPENDED hoặc trạng thái khác, khi verify sẽ chuyển sang APPROVED
    shop.setStatus(Shop.ShopStatus.APPROVED);
    Shop saved = shopRepository.save(shop);
    return toDTO(saved);
    }

    private ShopDTO toDTO(Shop shop) {
        if (shop == null) return null;
        ShopDTO dto = new ShopDTO();
        dto.setId(shop.getId());
        if (shop.getOwner() != null) {
            dto.setOwnerId(shop.getOwner().getId());
            dto.setOwnerName(shop.getOwner().getUsername());
        }
        dto.setName(shop.getName());
        dto.setAddress(shop.getAddress());
        dto.setDescription(shop.getDescription());
        dto.setStatus(shop.getStatus() != null ? shop.getStatus().name() : null);
        dto.setCreatedAt(shop.getCreatedAt());
        return dto;
    }

    private Shop toEntity(ShopDTO dto) {
        if (dto == null) return null;
        Shop shop = new Shop();
        shop.setName(dto.getName());
        shop.setAddress(dto.getAddress());
        shop.setDescription(dto.getDescription());
        // Không set status từ DTO khi tạo mới, luôn để mặc định (PENDING)
        return shop;
    }
}
