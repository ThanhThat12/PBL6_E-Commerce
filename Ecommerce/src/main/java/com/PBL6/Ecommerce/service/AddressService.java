package com.PBL6.Ecommerce.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.constant.TypeAddress;
import com.PBL6.Ecommerce.domain.dto.AddressRequestDTO;
import com.PBL6.Ecommerce.domain.entity.user.Address;
import com.PBL6.Ecommerce.domain.entity.user.User;
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

        // Primary logic ONLY for HOME addresses
        // STORE addresses don't have primary concept (only 1 store address per user)
        if (req.primaryAddress) {
            // Unset all other HOME addresses' primary flag
            addressRepository.findByUserId(userId).stream()
                    .filter(addr -> addr.getTypeAddress() == TypeAddress.HOME && addr.isPrimaryAddress())
                    .forEach(prev -> {
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
        a.setTypeAddress(TypeAddress.HOME);
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

        // Primary logic ONLY for HOME addresses
        if (req.primaryAddress && !a.isPrimaryAddress() && a.getTypeAddress() == TypeAddress.HOME) {
            // Unset all other HOME addresses' primary flag
            addressRepository.findByUserId(userId).stream()
                    .filter(addr -> addr.getTypeAddress() == TypeAddress.HOME 
                                 && addr.isPrimaryAddress() 
                                 && !addr.getId().equals(a.getId()))
                    .forEach(prev -> {
                        prev.setPrimaryAddress(false);
                        addressRepository.save(prev);
                    });
        }
        
        if (req.fullAddress != null) a.setFullAddress(req.fullAddress);
        if (req.provinceId != null) a.setProvinceId(req.provinceId);
        if (req.districtId != null) a.setDistrictId(req.districtId);
        if (req.wardCode != null) a.setWardCode(req.wardCode);
        
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
        
        // Handle primary address logic for HOME addresses only
        if (req.primaryAddress && !a.isPrimaryAddress() && a.getTypeAddress() == TypeAddress.HOME) {
            // Unset all other HOME addresses' primary flag
            addressRepository.findByUserId(userId).stream()
                    .filter(addr -> addr.getTypeAddress() == TypeAddress.HOME 
                                 && addr.isPrimaryAddress() 
                                 && !addr.getId().equals(addressId))
                    .forEach(prev -> {
                        prev.setPrimaryAddress(false);
                        addressRepository.save(prev);
                    });
        }
        
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
        
        // Cannot delete primary address
        if (a.isPrimaryAddress()) {
            throw new IllegalArgumentException("Không thể xóa địa chỉ mặc định. Vui lòng đặt địa chỉ khác làm mặc định trước.");
        }
        
        // Cannot delete STORE address (only update allowed)
        if (a.getTypeAddress() == TypeAddress.STORE) {
            throw new IllegalArgumentException("Không thể xóa địa chỉ cửa hàng. Chỉ có thể cập nhật địa chỉ này.");
        }
        
        addressRepository.delete(a);
    }

    @Transactional
    public Address markPrimary(Long userId, Long addressId) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        
        if (!a.getUser().getId().equals(userId)) {
            throw new UnauthorizedAddressAccessException(addressId);
        }

        // Primary concept ONLY applies to HOME addresses
        // STORE addresses cannot be marked as primary (only 1 store per user)
        if (a.getTypeAddress() != TypeAddress.HOME) {
            throw new IllegalArgumentException("Chỉ có địa chỉ HOME mới có thể được đặt làm mặc định. Địa chỉ STORE không cần đánh dấu mặc định.");
        }

        // Unset all other HOME addresses' primary flag
        addressRepository.findByUserId(userId).stream()
                .filter(addr -> addr.getTypeAddress() == TypeAddress.HOME 
                             && addr.isPrimaryAddress() 
                             && !addr.getId().equals(a.getId()))
                .forEach(prev -> {
                    prev.setPrimaryAddress(false);
                    addressRepository.save(prev);
                });
        
        a.setPrimaryAddress(true);
        return addressRepository.save(a);
    }
}