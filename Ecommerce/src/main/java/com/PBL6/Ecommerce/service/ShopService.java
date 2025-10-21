package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import java.util.List;

public interface ShopService {
    ShopDTO createShop(ShopDTO shopDTO);
    ShopDTO getShopById(Long id);
    List<ShopDTO> getShopsByOwnerId(Long ownerId);
    ShopDTO updateShop(Long id, ShopDTO shopDTO);
    void deleteShop(Long id);
    // Admin operations
    List<ShopDTO> listAllShops();
    ShopDTO approveShop(Long id);
    ShopDTO rejectShop(Long id);
    ShopDTO suspendShop(Long id);
    ShopDTO verifyShop(Long id);
}
