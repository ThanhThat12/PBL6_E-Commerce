package com.PBL6.Ecommerce.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.constant.TypeAddress;
import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AddressRequestDTO;
import com.PBL6.Ecommerce.exception.AddressNotFoundException;
import com.PBL6.Ecommerce.exception.UnauthorizedAddressAccessException;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final GhnMasterDataService ghnMaster;

    public AddressService(AddressRepository addressRepository,
                          UserRepository userRepository,
                          GhnMasterDataService ghnMaster) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.ghnMaster = ghnMaster;
    }

    public List<Address> listByUser(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address getByIdAndUser(Long id, Long userId) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedAddressAccessException(id);
        }
        
        return address;
    }

    private void resolveNamesIfNeeded(AddressRequestDTO req) {
        if ((req.provinceId == null || req.districtId == null || req.wardCode == null) &&
                ((req.provinceName != null && !req.provinceName.isBlank())
                 || (req.districtName != null && !req.districtName.isBlank())
                 || (req.wardName != null && !req.wardName.isBlank()))) {

            Map<String, Object> resolved = ghnMaster.resolveAddress(req.provinceName, req.districtName, req.wardName);
            if (resolved == null) return;

            Object p = resolved.get("province");
            if (p instanceof Map && req.provinceId == null) {
                Object pid = ((Map<?, ?>) p).get("id");
                if (pid instanceof Number) req.provinceId = ((Number) pid).intValue();
                else if (pid != null) {
                    try { req.provinceId = Integer.parseInt(String.valueOf(pid)); } catch (Exception ignored) {}
                }
            }
            Object d = resolved.get("district");
            if (d instanceof Map && req.districtId == null) {
                Object did = ((Map<?, ?>) d).get("id");
                if (did instanceof Number) req.districtId = ((Number) did).intValue();
                else if (did != null) {
                    try { req.districtId = Integer.parseInt(String.valueOf(did)); } catch (Exception ignored) {}
                }
            }
            Object w = resolved.get("ward");
            if (w instanceof Map && req.wardCode == null) {
                Object wcode = ((Map<?, ?>) w).get("code");
                if (wcode != null) req.wardCode = String.valueOf(wcode);
            }
        }
    }

    @Transactional
    public Address createForUser(Long userId, AddressRequestDTO req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        
        resolveNamesIfNeeded(req);
        
        // Parse and validate type
        TypeAddress type = TypeAddress.HOME;
        if (req.typeAddress != null && !req.typeAddress.isBlank()) {
            try {
                type = TypeAddress.valueOf(req.typeAddress.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Loại địa chỉ không hợp lệ. Chỉ chấp nhận: HOME hoặc STORE");
            }
        }
        
        // Business Rule: Seller chỉ có 1 địa chỉ STORE duy nhất
        if (type == TypeAddress.STORE) {
            Optional<Address> existingStore = addressRepository.findFirstByUserIdAndTypeAddress(userId, TypeAddress.STORE);
            if (existingStore.isPresent()) {
                throw new IllegalStateException("Bạn chỉ có thể có một địa chỉ cửa hàng. Vui lòng cập nhật địa chỉ hiện tại thay vì tạo mới.");
            }
            // STORE address không được set primary
            if (req.primaryAddress) {
                throw new IllegalArgumentException("Địa chỉ STORE không được đánh dấu là địa chỉ mặc định");
            }
        }
        
        // Business Rule: Primary address chỉ áp dụng cho HOME
        if (req.primaryAddress && type == TypeAddress.HOME) {
            addressRepository.findFirstByUserIdAndTypeAddress(userId, TypeAddress.HOME)
                    .ifPresent(prev -> {
                        prev.setPrimaryAddress(false);
                        addressRepository.save(prev);
                    });
        }

        Address a = new Address();
        a.setUser(user);
        a.setFullAddress(req.fullAddress);
        a.setProvinceId(req.provinceId);
        a.setDistrictId(req.districtId);
        a.setWardCode(req.wardCode);
        a.setTypeAddress(type);
        // Set names from request if provided, otherwise resolve from GHN
        if (req.provinceName != null && !req.provinceName.isBlank()) {
            a.setProvinceName(req.provinceName);
        }
        if (req.districtName != null && !req.districtName.isBlank()) {
            a.setDistrictName(req.districtName);
        }
        if (req.wardName != null && !req.wardName.isBlank()) {
            a.setWardName(req.wardName);
        }
        
        // Resolve and store location names from GHN if not provided
        resolveAndSetLocationNames(a);
        
    a.setContactName(req.contactName);
        a.setContactPhone(req.contactPhone);
        a.setPrimaryAddress(req.primaryAddress);
        return addressRepository.save(a);
    }

    private void resolveAndSetLocationNames(Address address) {
        // Resolve province name only if not already set
        if ((address.getProvinceName() == null || address.getProvinceName().isBlank()) 
            && address.getProvinceId() != null && address.getProvinceId() > 0) {
            try {
                System.out.println("Resolving province name for ID: " + address.getProvinceId());
                var provinces = ghnMaster.getProvinces();
                provinces.stream()
                    .filter(p -> address.getProvinceId().equals(p.get("ProvinceID")))
                    .findFirst()
                    .ifPresent(p -> {
                        String name = (String) p.get("ProvinceName");
                        System.out.println("Found province name: " + name);
                        address.setProvinceName(name);
                    });
            } catch (Exception e) {
                System.err.println("Error resolving province name: " + e.getMessage());
            }
        }
        
        // Resolve district name only if not already set
        if ((address.getDistrictName() == null || address.getDistrictName().isBlank())
            && address.getDistrictId() != null && address.getDistrictId() > 0 
            && address.getProvinceId() != null && address.getProvinceId() > 0) {
            try {
                System.out.println("Resolving district name for ID: " + address.getDistrictId() + ", provinceId: " + address.getProvinceId());
                var districts = ghnMaster.getDistricts(address.getProvinceId());
                districts.stream()
                    .filter(d -> address.getDistrictId().equals(d.get("DistrictID")))
                    .findFirst()
                    .ifPresent(d -> {
                        String name = (String) d.get("DistrictName");
                        System.out.println("Found district name: " + name);
                        address.setDistrictName(name);
                    });
            } catch (Exception e) {
                System.err.println("Error resolving district name: " + e.getMessage());
            }
        }
        
        // Resolve ward name only if not already set
        if ((address.getWardName() == null || address.getWardName().isBlank())
            && address.getWardCode() != null && !address.getWardCode().isBlank()
            && address.getDistrictId() != null && address.getDistrictId() > 0) {
            try {
                System.out.println("Resolving ward name for code: " + address.getWardCode() + ", districtId: " + address.getDistrictId());
                var wards = ghnMaster.getWards(address.getDistrictId());
                wards.stream()
                    .filter(w -> address.getWardCode().equals(String.valueOf(w.get("WardCode"))))
                    .findFirst()
                    .ifPresent(w -> {
                        String name = (String) w.get("WardName");
                        System.out.println("Found ward name: " + name);
                        address.setWardName(name);
                    });
            } catch (Exception e) {
                System.err.println("Error resolving ward name: " + e.getMessage());
            }
        }
    }

    @Transactional
    public Address updateForUser(Long userId, Long addressId, AddressRequestDTO req) {
        resolveNamesIfNeeded(req);

        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        
        if (!a.getUser().getId().equals(userId)) {
            throw new UnauthorizedAddressAccessException(addressId);
        }
        
        // Validate type change
        if (req.typeAddress != null && !req.typeAddress.isBlank()) {
            try {
                TypeAddress newType = TypeAddress.valueOf(req.typeAddress.toUpperCase());
                
                // Không cho phép đổi từ HOME sang STORE hoặc ngược lại
                if (a.getTypeAddress() != newType) {
                    throw new IllegalArgumentException("Không thể thay đổi loại địa chỉ từ " + a.getTypeAddress() + " sang " + newType);
                }
            } catch (IllegalArgumentException e) {
                if (!e.getMessage().contains("Không thể thay đổi")) {
                    throw new IllegalArgumentException("Loại địa chỉ không hợp lệ. Chỉ chấp nhận: HOME hoặc STORE");
                }
                throw e;
            }
        }
        
        // Validate primary for STORE
        if (req.primaryAddress && a.getTypeAddress() == TypeAddress.STORE) {
            throw new IllegalArgumentException("Địa chỉ STORE không được đánh dấu là địa chỉ mặc định");
        }

        // Unset other primary addresses if setting this as primary
        if (req.primaryAddress && !a.isPrimaryAddress() && a.getTypeAddress() == TypeAddress.HOME) {
            addressRepository.findFirstByUserIdAndTypeAddress(userId, TypeAddress.HOME)
                    .ifPresent(prev -> {
                        if (!prev.getId().equals(a.getId())) {
                            prev.setPrimaryAddress(false);
                            addressRepository.save(prev);
                        }
                    });
        }
        
        if (req.fullAddress != null) a.setFullAddress(req.fullAddress);
        if (req.provinceId != null) a.setProvinceId(req.provinceId);
        if (req.districtId != null) a.setDistrictId(req.districtId);
        if (req.wardCode != null) a.setWardCode(req.wardCode);
        // Note: Type address cannot be changed after creation
        
        // Set names from request if provided
        if (req.provinceName != null && !req.provinceName.isBlank()) {
            a.setProvinceName(req.provinceName);
        }
        if (req.districtName != null && !req.districtName.isBlank()) {
            a.setDistrictName(req.districtName);
        }
        if (req.wardName != null && !req.wardName.isBlank()) {
            a.setWardName(req.wardName);
        }
        
        // Resolve and update location names if IDs changed
        resolveAndSetLocationNames(a);
        
    if (req.contactName != null) a.setContactName(req.contactName);
    if (req.contactPhone != null) a.setContactPhone(req.contactPhone);
        a.setPrimaryAddress(req.primaryAddress);
        return addressRepository.save(a);
    }

    @Transactional
    public void deleteForUser(Long userId, Long addressId) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        
        if (!a.getUser().getId().equals(userId)) {
            throw new UnauthorizedAddressAccessException(addressId);
        }
        
        // Business Rule: Không cho xóa địa chỉ primary (phải set primary cho địa chỉ khác trước)
        if (a.isPrimaryAddress() && a.getTypeAddress() == TypeAddress.HOME) {
            throw new IllegalStateException("Không thể xóa địa chỉ mặc định. Vui lòng đặt địa chỉ khác làm mặc định trước khi xóa.");
        }
        
        // Business Rule: Không cho xóa địa chỉ STORE (chỉ cho phép update)
        if (a.getTypeAddress() == TypeAddress.STORE) {
            throw new IllegalStateException("Không thể xóa địa chỉ cửa hàng. Bạn chỉ có thể cập nhật thông tin.");
        }
        
        addressRepository.delete(a);
    }

    /**
     * Mark address as primary - CHỈ áp dụng cho địa chỉ HOME
     * Business Rule:
     * - Chỉ địa chỉ HOME mới có thể set primary
     * - Tự động unset primary cho các địa chỉ HOME khác của user
     * - STORE address không có primary
     */
    @Transactional
    public Address markPrimary(Long userId, Long addressId) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        
        if (!a.getUser().getId().equals(userId)) {
            throw new UnauthorizedAddressAccessException(addressId);
        }
        
        // Validate: Chỉ HOME address mới có thể set primary
        if (a.getTypeAddress() != TypeAddress.HOME) {
            throw new IllegalArgumentException("Chỉ địa chỉ nhận hàng (HOME) mới có thể đặt làm mặc định. Địa chỉ cửa hàng (STORE) không cần đặt mặc định.");
        }

        // Unset primary for other HOME addresses
        List<Address> homeAddresses = addressRepository.findByUserId(userId).stream()
                .filter(addr -> addr.getTypeAddress() == TypeAddress.HOME && addr.isPrimaryAddress() && !addr.getId().equals(addressId))
                .collect(java.util.stream.Collectors.toList());
        
        for (Address prev : homeAddresses) {
            prev.setPrimaryAddress(false);
            addressRepository.save(prev);
        }
        
        a.setPrimaryAddress(true);
        return addressRepository.save(a);
    }
    
    /**
     * Get primary HOME address for user (for checkout)
     */
    public Address getPrimaryHomeAddress(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .filter(a -> a.getTypeAddress() == TypeAddress.HOME && a.isPrimaryAddress())
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get STORE address for seller (for shipping from_address)
     */
    public Address getStoreAddress(Long sellerId) {
        return addressRepository.findFirstByUserIdAndTypeAddress(sellerId, TypeAddress.STORE)
                .orElse(null);
    }
}