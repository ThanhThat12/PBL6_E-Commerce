package com.PBL6.Ecommerce.controller.shop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ShopDTO;
import com.PBL6.Ecommerce.service.ShopService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Public Shop Controller - APIs công khai cho homepage
 * GET /api/public/shops/featured - Lấy shop nổi bật
 */
@RestController
@RequestMapping("/api/public/shops")
@Tag(name = "Public Shops", description = "Public APIs for shop discovery")
public class PublicShopController {
    
    private static final Logger log = LoggerFactory.getLogger(PublicShopController.class);
    
    private final ShopService shopService;

    public PublicShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    /**
     * Lấy shop nổi bật (featured shops)
     * GET /api/public/shops/featured?page=0&size=6
     * Criteria: ACTIVE shops, sorted by total sales/rating
     */
    @Operation(
        summary = "Get featured shops for homepage",
        description = "Get active featured shops sorted by performance metrics"
    )
    @GetMapping("/featured")
    public ResponseEntity<ResponseDTO<Page<ShopDTO>>> getFeaturedShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ShopDTO> shops = shopService.getFeaturedShops(pageable);
            
            log.info("Found {} featured shops for homepage", shops.getTotalElements());
            
            return ResponseDTO.success(shops, "Lấy danh sách shop nổi bật thành công");
        } catch (Exception e) {
            log.error("Error getting featured shops", e);
            return ResponseDTO.error(400, "GET_FEATURED_SHOPS_ERROR", e.getMessage());
        }
    }
}
