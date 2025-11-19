package com.PBL6.Ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.PBL6.Ecommerce.domain.dto.*;
import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.constant.TypeAddress;
import com.PBL6.Ecommerce.service.GhnService;
import com.PBL6.Ecommerce.repository.*;

import java.util.*;
import java.math.BigDecimal;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import com.PBL6.Ecommerce.domain.User;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
    private final GhnService ghnService;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository;
    private final com.PBL6.Ecommerce.repository.ProductVariantRepository productVariantRepository;
    private final com.PBL6.Ecommerce.repository.CartItemRepository cartItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public CheckoutController(GhnService ghnService, 
                            AddressRepository addressRepository,
                            ShopRepository shopRepository,
                            com.PBL6.Ecommerce.repository.ProductVariantRepository productVariantRepository,
                            com.PBL6.Ecommerce.repository.CartItemRepository cartItemRepository,
                            ShipmentRepository shipmentRepository,
                            UserRepository userRepository) {
        this.ghnService = ghnService;
        this.addressRepository = addressRepository;
        this.shopRepository = shopRepository;
        this.productVariantRepository = productVariantRepository;
        this.cartItemRepository = cartItemRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Bước 1-6: User ấn thanh toán → Backend trả danh sách dịch vụ GHN
     * POST /api/checkout/available-services
     */
    @PostMapping("/available-services")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<List<Map<String,Object>>>> getAvailableServices(
            @Valid @RequestBody CheckoutInitRequestDTO req) {
        try {
            // 2. Lấy địa chỉ HOME của buyer
            Address buyerAddress = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
            
            if (buyerAddress.getTypeAddress() != TypeAddress.HOME) {
                throw new RuntimeException("Địa chỉ phải là loại HOME");
            }

            // 3. Tìm địa chỉ STORE của shop
            Shop shop = shopRepository.findById(req.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            Address shopAddress = addressRepository.findByUserAndTypeAddress(
                shop.getOwner(), TypeAddress.STORE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ STORE của shop"));

            // 4. Tính tổng trọng lượng từ cartItems
            int totalWeight = 0;
            if (req.getCartItemIds() != null && req.getCartItemIds().length > 0) {
                var cartItems = cartItemRepository.findAllById(Arrays.asList(req.getCartItemIds()));
                for (var item : cartItems) {
                    Integer weight = item.getProductVariant().getProduct().getWeightGrams();
                    if (weight != null) {
                        totalWeight += weight * item.getQuantity();
                    }
                }
            }
            
            if (totalWeight == 0) {
                totalWeight = 200; // default 200g nếu không có weight
            }

            // 5. Gọi GHN /available-services
            Map<String, Object> payload = new HashMap<>();
            // ❌ Không cần put shop_id nữa - GhnService sẽ tự thêm
            // payload.put("shop_id", Integer.parseInt(shop.getGhnShopId()));
            
            payload.put("from_district", shopAddress.getDistrictId());
            payload.put("to_district", buyerAddress.getDistrictId());
            
            List<Map<String,Object>> services = ghnService.getAvailableServices(payload, req.getShopId());
            
            // 6. Trả về danh sách dịch vụ
            Map<String,Object> response = new HashMap<>();
            response.put("services", services);
            response.put("totalWeight", totalWeight);
            response.put("shopAddress", Map.of(
                "districtId", shopAddress.getDistrictId(),
                "wardCode", shopAddress.getWardCode()
            ));
            response.put("buyerAddress", Map.of(
                "districtId", buyerAddress.getDistrictId(),
                "wardCode", buyerAddress.getWardCode()
            ));
            
            return ResponseDTO.ok(List.of(response), "Lấy danh sách dịch vụ thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Bước 7-10: User chọn service → Backend tính phí ship
     * POST /api/checkout/calculate-fee
     */
    @PostMapping("/calculate-fee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Map<String,Object>>> calculateShippingFee(
            @Valid @RequestBody CheckoutCalculateFeeRequestDTO req) {
        try {
            // Lấy địa chỉ HOME của buyer
            Address buyerAddress = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
            
            if (buyerAddress.getTypeAddress() != TypeAddress.HOME) {
                throw new RuntimeException("Địa chỉ phải là loại HOME");
            }

            // Tìm địa chỉ STORE của shop
            Shop shop = shopRepository.findById(req.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            Address shopAddress = addressRepository.findByUserAndTypeAddress(
                shop.getOwner(), TypeAddress.STORE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ STORE của shop"));

            // Tính tổng trọng lượng và lấy kích thước
            int totalWeight = 0;
            int maxLength = 0, maxWidth = 0, maxHeight = 0;
            List<Map<String, Object>> items = new ArrayList<>();
            
            if (req.getCartItemIds() != null && req.getCartItemIds().length > 0) {
                var cartItems = cartItemRepository.findAllById(Arrays.asList(req.getCartItemIds()));
                
                for (var cartItem : cartItems) {
                    var product = cartItem.getProductVariant().getProduct();
                    int quantity = cartItem.getQuantity();
                    
                    // Tính weight
                    Integer weight = product.getWeightGrams();
                    if (weight != null) {
                        totalWeight += weight * quantity;
                    }
                    
                    // Lấy kích thước lớn nhất
                    if (product.getLengthCm() != null) {
                        maxLength = Math.max(maxLength, product.getLengthCm());
                    }
                    if (product.getWidthCm() != null) {
                        maxWidth = Math.max(maxWidth, product.getWidthCm());
                    }
                    if (product.getHeightCm() != null) {
                        maxHeight = Math.max(maxHeight, product.getHeightCm());
                    }
                    
                    // Tạo items array theo format GHN
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", product.getName());
                    item.put("quantity", quantity);
                    item.put("weight", weight != null ? weight : 200);
                    item.put("length", product.getLengthCm() != null ? product.getLengthCm() : 20);
                    item.put("width", product.getWidthCm() != null ? product.getWidthCm() : 20);
                    item.put("height", product.getHeightCm() != null ? product.getHeightCm() : 10);
                    items.add(item);
                }
            }
            
            // Default values nếu không có dữ liệu
            if (totalWeight == 0) totalWeight = 200;
            if (maxLength == 0) maxLength = 20;
            if (maxWidth == 0) maxWidth = 20;
            if (maxHeight == 0) maxHeight = 10;
            if (items.isEmpty()) {
                items.add(Map.of(
                    "name", "Sản phẩm",
                    "quantity", 1,
                    "weight", totalWeight,
                    "length", maxLength,
                    "width", maxWidth,
                    "height", maxHeight
                ));
            }

            // Gọi GHN /shipping-order/fee
            Map<String, Object> payload = new HashMap<>();
            
            // Required fields
            payload.put("from_district_id", shopAddress.getDistrictId());
            payload.put("from_ward_code", shopAddress.getWardCode());
            payload.put("to_district_id", buyerAddress.getDistrictId());
            payload.put("to_ward_code", buyerAddress.getWardCode());
            payload.put("service_id", req.getServiceId());
            
            // service_type_id có thể null theo doc
            if (req.getServiceTypeId() != null) {
                payload.put("service_type_id", req.getServiceTypeId());
            }
            
            // Package dimensions
            payload.put("weight", totalWeight);
            payload.put("length", maxLength);
            payload.put("width", maxWidth);
            payload.put("height", maxHeight);
            
            // Items array
            payload.put("items", items);
            
            // Optional fields (có thể thêm sau)
            payload.put("insurance_value", 0); // Giá trị bảo hiểm
            payload.put("coupon", null);
            
            Map<String,Object> feeResponse = ghnService.calculateFee(payload, req.getShopId());
            
            return ResponseDTO.ok(feeResponse, "Tính phí thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Bước 11-13: User xác nhận thanh toán → Tạo Order + GHN shipment
     * POST /api/checkout/confirm
     */
    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Map<String,Object>>> confirmCheckout(
        @Valid @RequestBody CheckoutConfirmRequestDTO req,
        @AuthenticationPrincipal Jwt jwt) {
    try {

        // Lấy email từ token
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            throw new RuntimeException("Token không có email");
        }

        // Lấy User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Lấy địa chỉ HOME của buyer
            Address buyerAddress = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
            
            if (buyerAddress.getTypeAddress() != TypeAddress.HOME) {
                throw new RuntimeException("Địa chỉ phải là loại HOME");
            }

            // Tìm shop
            Shop shop = shopRepository.findById(req.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            // Địa chỉ STORE của shop
            Address shopAddress = addressRepository.findByUserAndTypeAddress(
                shop.getOwner(), TypeAddress.STORE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ STORE của shop"));

            // Lấy cart items và validate
            List<Long> requestedIds = Arrays.asList(req.getCartItemIds());
            var cartItems = cartItemRepository.findAllById(requestedIds);
            
            if (cartItems.isEmpty()) {
                throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng");
            }

            // Tính tổng trọng lượng, dimensions và items
            int totalWeight = 0;
            int maxLength = 0, maxWidth = 0, maxHeight = 0;
            int codAmount = 0;  // Tổng tiền thu hộ (nếu COD)
            List<Map<String, Object>> items = new ArrayList<>();
            
            for (var cartItem : cartItems) {
                var product = cartItem.getProductVariant().getProduct();
                var variant = cartItem.getProductVariant();
                int quantity = cartItem.getQuantity();
                
                // Weight
                Integer weight = product.getWeightGrams();
                if (weight != null) {
                    totalWeight += weight * quantity;
                }
                
                // Dimensions
                if (product.getLengthCm() != null) {
                    maxLength = Math.max(maxLength, product.getLengthCm());
                }
                if (product.getWidthCm() != null) {
                    maxWidth = Math.max(maxWidth, product.getWidthCm());
                }
                if (product.getHeightCm() != null) {
                    maxHeight = Math.max(maxHeight, product.getHeightCm());
                }
                
                // ✅ SỬA: Lấy giá từ variant thay vì product
                // COD amount (nếu thanh toán COD)
                if ("COD".equalsIgnoreCase(req.getPaymentMethod())) {
                    BigDecimal price = variant.getPrice(); // Lấy từ variant
                    if (price != null) {
                        codAmount += price.intValue() * quantity;
                    }
                }
                
                // ✅ SỬA: Items array - dùng giá từ variant
                Map<String, Object> item = new HashMap<>();
                item.put("name", product.getName());
                item.put("code", variant.getSku() != null ? variant.getSku() : "");
                item.put("quantity", quantity);
                item.put("price", variant.getPrice() != null ? variant.getPrice().intValue() : 0);
                item.put("length", product.getLengthCm() != null ? product.getLengthCm() : 12);
                item.put("width", product.getWidthCm() != null ? product.getWidthCm() : 12);
                item.put("height", product.getHeightCm() != null ? product.getHeightCm() : 12);
                item.put("weight", weight != null ? weight : 200);
                
                // Category (optional)
                Map<String, String> category = new HashMap<>();
                category.put("level1", product.getCategory() != null ? 
                    product.getCategory().getName() : "Khác");
                item.put("category", category);
                
                items.add(item);
            }
            
            // Default values nếu thiếu
            if (totalWeight == 0) totalWeight = 200;
            if (maxLength == 0) maxLength = 20;
            if (maxWidth == 0) maxWidth = 20;
            if (maxHeight == 0) maxHeight = 10;

            // ========== Tạo payload cho GHN ==========
            Map<String, Object> payload = new HashMap<>();
            
            // ✅ SỬA: Thông tin người gửi (shop)
            payload.put("from_name", shopAddress.getContactName() != null ? 
                shopAddress.getContactName() : shop.getName());
            // ✅ SỬA: Thay getPhone() bằng getPhoneNumber()
            payload.put("from_phone", shopAddress.getContactPhone() != null ? 
                shopAddress.getContactPhone() : shop.getOwner().getPhoneNumber());
            payload.put("from_address", shopAddress.getFullAddress());
            payload.put("from_ward_name", shopAddress.getWardName());
            payload.put("from_district_name", shopAddress.getDistrictName());
            payload.put("from_province_name", shopAddress.getProvinceName());
            
            // ✅ SỬA: Địa chỉ trả hàng (return address)
            payload.put("return_phone", shopAddress.getContactPhone() != null ? 
                shopAddress.getContactPhone() : shop.getOwner().getPhoneNumber());
            payload.put("return_address", shopAddress.getFullAddress());
            payload.put("return_district_id", null);
            payload.put("return_ward_code", "");
            
            // ✅ SỬA: Thông tin người nhận (buyer)
            payload.put("to_name", buyerAddress.getContactName() != null ? 
                buyerAddress.getContactName() : user.getFullName());
            // ✅ SỬA: Thay getPhone() bằng getPhoneNumber()
            payload.put("to_phone", buyerAddress.getContactPhone() != null ? 
                buyerAddress.getContactPhone() : user.getPhoneNumber());
            payload.put("to_address", buyerAddress.getFullAddress());
            payload.put("to_ward_code", buyerAddress.getWardCode());
            payload.put("to_district_id", buyerAddress.getDistrictId());
            
            // ✅ Thông tin đơn hàng
            payload.put("weight", totalWeight);
            payload.put("length", maxLength);
            payload.put("width", maxWidth);
            payload.put("height", maxHeight);
            
            payload.put("service_id", req.getServiceId());
            payload.put("service_type_id", req.getServiceTypeId());
            
            // ✅ Payment type: 1=Shop trả ship, 2=Buyer trả ship
            payload.put("payment_type_id", 2);
            
            // ✅ Required note
            payload.put("required_note", "KHONGCHOXEMHANG");
            
            // ✅ COD amount - Số tiền thu hộ
            payload.put("cod_amount", codAmount);
            
            // ✅ Insurance value - Giá trị bảo hiểm (10% của cod_amount)
            payload.put("insurance_value", codAmount > 0 ? (int)(codAmount * 0.1) : 0);
            
            // ✅ Items array
            payload.put("items", items);
            
            // ✅ Optional fields
            payload.put("client_order_code", "");  // Mã đơn hàng của shop (để trống)
            payload.put("note", req.getNote() != null ? req.getNote() : "");
            payload.put("content", "Đơn hàng từ " + shop.getName());
            payload.put("coupon", null);
            
            // Pick shift - Ca lấy hàng: [2] = chiều
            payload.put("pick_shift", new int[]{2});
            
            // Pick/Deliver station
            payload.put("pick_station_id", shopAddress.getDistrictId());
            payload.put("deliver_station_id", null);
            
            // Debug log
            System.out.println("========== CREATE ORDER PAYLOAD ==========");
            System.out.println("From: " + payload.get("from_name") + " - " + payload.get("from_phone"));
            System.out.println("To: " + payload.get("to_name") + " - " + payload.get("to_phone"));
            System.out.println("Weight: " + totalWeight + "g");
            System.out.println("COD Amount: " + codAmount);
            System.out.println("Items: " + items.size());
            System.out.println("Full Payload: " + payload);
            System.out.println("==========================================");
            
            // Gọi GHN API
            Map<String,Object> ghnResponse = ghnService.createShippingOrder(payload, req.getShopId());
            
            // TODO: Tạo Order trong DB và lưu order_code từ GHN
            
            return ResponseDTO.ok(ghnResponse, "Tạo đơn hàng thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi tạo đơn", null));
        }
    }
}
