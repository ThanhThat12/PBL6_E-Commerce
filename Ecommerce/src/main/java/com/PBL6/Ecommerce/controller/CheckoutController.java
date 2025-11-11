package com.PBL6.Ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.CheckoutShippingRequestDTO;
import com.PBL6.Ecommerce.domain.dto.CheckoutFeeRequestDTO;
import com.PBL6.Ecommerce.service.GhnService;
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Objects;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helper endpoints used during checkout
 */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
 private final GhnService ghnService;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository; // added
    private final com.PBL6.Ecommerce.repository.ProductVariantRepository productVariantRepository;

    public CheckoutController(GhnService ghnService, AddressRepository addressRepository,
                              ShopRepository shopRepository,
                              com.PBL6.Ecommerce.repository.ProductVariantRepository productVariantRepository) {
        this.ghnService = ghnService;
        this.addressRepository = addressRepository;
        this.shopRepository = shopRepository; // added
        this.productVariantRepository = productVariantRepository;
    }
    /**
     * Compute GHN available services for a checkout scenario.
     * Frontend can provide buyer's addressId (existing Address) and the items to calculate weight.
     * Body: { shopId, addressId, items:[{variantId,quantity}] }
     * Returns GHN response (wrapped in ResponseDTO).
     */
    // ...existing code...
    @PostMapping("/shipping-options")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseDTO<Map<String,Object>>> getShippingOptions(@Valid @RequestBody CheckoutShippingRequestDTO req) {
        try {
            // resolve buyer address (to)
            Integer toDistrict = null;
            String toWard = null;
            String ghnShopExternalId = null;
            if (req.getAddressId() != null) {
                var addr = addressRepository.findById(req.getAddressId())
                        .orElseThrow(() -> new IllegalArgumentException("Address not found: " + req.getAddressId()));
                if (addr.getDistrictId() != null) toDistrict = addr.getDistrictId();
                if (addr.getWardCode() != null) toWard = addr.getWardCode();
            }

            // resolve shop pickup (from)
            Integer fromDistrict = null;
            String fromWard = null;
            Long shopId = req.getShopId() == null ? null : Long.valueOf(req.getShopId());
            if (shopId != null) {
                try {
                    var shop = shopRepository.findById(shopId).orElse(null);
                    if (shop != null) {
                        // lấy pickup address nếu có
                        if (shop.getPickupAddress() != null) {
                            var pickup = addressRepository.findById(shop.getPickupAddress().getId()).orElse(null);
                            if (pickup != null) {
                                if (pickup.getDistrictId() != null) fromDistrict = pickup.getDistrictId();
                                if (pickup.getWardCode() != null) fromWard = pickup.getWardCode();
                            }
                        }
                        // lấy GHN external shop id (nếu lưu trong DB)
                        try { if (shop.getGhnShopId() != null && !shop.getGhnShopId().isBlank()) ghnShopExternalId = shop.getGhnShopId(); }
                        catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }

            // compute subtotal and weight
            int totalWeight = 0;
            BigDecimal subtotal = BigDecimal.ZERO;
            if (req.getItems() != null && !req.getItems().isEmpty()) {
                List<Long> variantIds = req.getItems().stream()
                        .map(i -> i.getVariantId())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                var variants = productVariantRepository.findAllById(variantIds);
                Map<Long, com.PBL6.Ecommerce.domain.ProductVariant> variantMap = new HashMap<>();
                for (var v : variants) variantMap.put(v.getId(), v);

                
 
                final double divisor = 5000.0;
                for (var it : req.getItems()) {
                    var v = variantMap.get(it.getVariantId());
                    int qty = it.getQuantity() == null ? 1 : it.getQuantity();
                    if (v != null) {
                        var p = v.getProduct();
                        int actual = p != null && p.getWeightGrams() != null ? p.getWeightGrams() : 0;
                        int volumetric = 0;
                        if (p != null && p.getLengthCm() != null && p.getWidthCm() != null && p.getHeightCm() != null) {
                            double volKg = (p.getLengthCm() * (double) p.getWidthCm() * p.getHeightCm()) / divisor;
                            volumetric = (int) Math.ceil(volKg * 1000.0);
                        }
                        int unit = Math.max(actual, volumetric);
                        totalWeight += unit * qty;

                        if (v.getPrice() != null) subtotal = subtotal.add(v.getPrice().multiply(BigDecimal.valueOf(qty)));
                    }
                }
            }

            final int MIN_WEIGHT = 100; // grams
            int effectiveWeight = totalWeight > 0 ? totalWeight : MIN_WEIGHT;
            // build payload
            Map<String,Object> payload = new HashMap<>();
            if (toDistrict != null) {
                payload.put("to_district_id", toDistrict);
                payload.put("ToDistrictID", toDistrict);
            }
            if (toWard != null) {
                payload.put("to_ward_code", toWard);
                payload.put("ToWardCode", toWard);
            }
            if (fromDistrict != null) {
                payload.put("from_district_id", fromDistrict);
                payload.put("FromDistrictID", fromDistrict);
            }
            if (fromWard != null) {
                payload.put("from_ward_code", fromWard);
                payload.put("FromWardCode", fromWard);
            }

            payload.put("weight", effectiveWeight);
            payload.put("Weight", effectiveWeight);

            // insurance/subtotal
            payload.put("insurance_value", subtotal.intValue());

            // ensure ShopID present for GHN (prefer external GHN id stored on Shop)
            if (ghnShopExternalId != null) {
                payload.put("ShopID", ghnShopExternalId);
                try { payload.put("shop_id", Integer.parseInt(ghnShopExternalId)); } catch (Exception e) { payload.put("shop_id", ghnShopExternalId); }
            } else if (shopId != null) {
                payload.put("shop_id", shopId);
                payload.put("ShopID", String.valueOf(shopId));
            }
            Map<String,Object> resp = ghnService.getAvailableServices(payload, shopId);
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Lấy phương án vận chuyển thành công", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new ResponseDTO<>(400, e.getMessage(), "Lấy phương án thất bại", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null));
        }
    }
// ...existing code...

    /**
     * Calculate GHN fee for a chosen service in checkout flow.
     * POST /api/checkout/calculate-fee
     */
    @PostMapping("/calculate-fee")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseDTO<Map<String,Object>>> calculateFee(@Valid @RequestBody CheckoutFeeRequestDTO req) {
        try {
            // resolve buyer address
            Integer toDistrict = null;
            String toWard = null;
            if (req.getAddressId() != null) {
                var addr = addressRepository.findById(req.getAddressId())
                        .orElseThrow(() -> new IllegalArgumentException("Address not found: " + req.getAddressId()));
                if (addr.getDistrictId() != null) toDistrict = addr.getDistrictId();
                if (addr.getWardCode() != null) toWard = addr.getWardCode();
            }

            // compute subtotal and weight
            List<Long> variantIds = req.getItems().stream().map(i -> i.getVariantId()).collect(Collectors.toList());
            var variants = productVariantRepository.findAllById(variantIds);
            Map<Long, com.PBL6.Ecommerce.domain.ProductVariant> variantMap = new HashMap<>();
            for (var v : variants) variantMap.put(v.getId(), v);

            int totalWeight = 0;
            BigDecimal subtotal = BigDecimal.ZERO;
            final double divisor = 5000.0;
            for (var it : req.getItems()) {
                var v = variantMap.get(it.getVariantId());
                int qty = it.getQuantity() == null ? 1 : it.getQuantity();
                if (v != null) {
                    var p = v.getProduct();
                    int actual = p != null && p.getWeightGrams() != null ? p.getWeightGrams() : 0;
                    int volumetric = 0;
                    if (p != null && p.getLengthCm() != null && p.getWidthCm() != null && p.getHeightCm() != null) {
                        double volKg = (p.getLengthCm() * (double) p.getWidthCm() * p.getHeightCm()) / divisor;
                        volumetric = (int) Math.ceil(volKg * 1000.0);
                    }
                    int unit = Math.max(actual, volumetric);
                    totalWeight += unit * qty;

                    if (v.getPrice() != null) subtotal = subtotal.add(v.getPrice().multiply(BigDecimal.valueOf(qty)));
                }
            }

            Map<String,Object> payload = new HashMap<>();
            if (toDistrict != null) payload.put("to_district_id", toDistrict);
            if (toWard != null) payload.put("to_ward_code", toWard);
            if (totalWeight > 0) payload.put("weight", totalWeight);
            payload.put("insurance_value", subtotal.intValue());
            payload.put("cod_amount", req.getCodAmount() != null ? req.getCodAmount() : 0);
            if (req.getServiceId() != null) payload.put("service_id", req.getServiceId());
            if (req.getServiceTypeId() != null) payload.put("service_type_id", req.getServiceTypeId());

            Map<String,Object> resp = ghnService.calculateFee(payload, req.getShopId());
            return ResponseEntity.ok(new ResponseDTO<>(200, null, "Tính phí vận chuyển thành công", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new ResponseDTO<>(400, e.getMessage(), "Tính phí thất bại", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseDTO<>(500, e.getMessage(), "Lỗi hệ thống", null));
        }
    }
}
