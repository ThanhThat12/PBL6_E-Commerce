package com.PBL6.Ecommerce.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if (req.primaryAddress) {
            addressRepository.findByUserIdAndPrimaryAddressTrue(userId)
                    .ifPresent(prev -> {
                        prev.setPrimaryAddress(false);
                        addressRepository.save(prev);
                    });
        }

        Address a = new Address();
        a.setUser(user);
        a.setLabel(req.label);
        a.setFullAddress(req.fullAddress);
        a.setProvinceId(req.provinceId);
        a.setDistrictId(req.districtId);
        a.setWardCode(req.wardCode);
        
        // Resolve and store location names
        resolveAndSetLocationNames(a);
        
        a.setContactPhone(req.contactPhone);
        a.setPrimaryAddress(req.primaryAddress);
        return addressRepository.save(a);
    }

    private void resolveAndSetLocationNames(Address address) {
        // Resolve province name
        if (address.getProvinceId() != null) {
            try {
                var provinces = ghnMaster.getProvinces();
                provinces.stream()
                    .filter(p -> address.getProvinceId().equals(p.get("ProvinceID")))
                    .findFirst()
                    .ifPresent(p -> address.setProvinceName((String) p.get("ProvinceName")));
            } catch (Exception ignored) {}
        }
        
        // Resolve district name
        if (address.getDistrictId() != null && address.getProvinceId() != null) {
            try {
                var districts = ghnMaster.getDistricts(address.getProvinceId());
                districts.stream()
                    .filter(d -> address.getDistrictId().equals(d.get("DistrictID")))
                    .findFirst()
                    .ifPresent(d -> address.setDistrictName((String) d.get("DistrictName")));
            } catch (Exception ignored) {}
        }
        
        // Resolve ward name
        if (address.getWardCode() != null && address.getDistrictId() != null) {
            try {
                var wards = ghnMaster.getWards(address.getDistrictId());
                wards.stream()
                    .filter(w -> address.getWardCode().equals(w.get("WardCode")))
                    .findFirst()
                    .ifPresent(w -> address.setWardName((String) w.get("WardName")));
            } catch (Exception ignored) {}
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

        if (req.primaryAddress && !a.isPrimaryAddress()) {
            addressRepository.findByUserIdAndPrimaryAddressTrue(userId)
                    .ifPresent(prev -> {
                        if (!prev.getId().equals(a.getId())) {
                            prev.setPrimaryAddress(false);
                            addressRepository.save(prev);
                        }
                    });
        }
        
        if (req.label != null) a.setLabel(req.label);
        if (req.fullAddress != null) a.setFullAddress(req.fullAddress);
        if (req.provinceId != null) a.setProvinceId(req.provinceId);
        if (req.districtId != null) a.setDistrictId(req.districtId);
        if (req.wardCode != null) a.setWardCode(req.wardCode);
        
        // Resolve and update location names if IDs changed
        resolveAndSetLocationNames(a);
        
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
        
        addressRepository.delete(a);
    }

    @Transactional
    public Address markPrimary(Long userId, Long addressId) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        
        if (!a.getUser().getId().equals(userId)) {
            throw new UnauthorizedAddressAccessException(addressId);
        }

        addressRepository.findByUserIdAndPrimaryAddressTrue(userId)
                .ifPresent(prev -> {
                    if (!prev.getId().equals(a.getId())) {
                        prev.setPrimaryAddress(false);
                        addressRepository.save(prev);
                    }
                });
        
        a.setPrimaryAddress(true);
        return addressRepository.save(a);
    }
}