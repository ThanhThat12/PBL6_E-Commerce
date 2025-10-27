package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AddressRequestDTO;
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public Optional<Address> getByIdAndUser(Long id, Long userId) {
        return addressRepository.findByIdAndUserId(id, userId);
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
    public Optional<Address> createForUser(Long userId, AddressRequestDTO req) {
        return userRepository.findById(userId).map(user -> {
            resolveNamesIfNeeded(req);

            if (req.primaryAddress) {
                addressRepository.findByUserIdAndPrimaryAddressTrue(userId)
                        .ifPresent(prev -> { prev.setPrimaryAddress(false); addressRepository.save(prev); });
            }

            Address a = new Address();
            a.setUser(user);
            a.setLabel(req.label);
            a.setFullAddress(req.fullAddress);
            a.setProvinceId(req.provinceId);
            a.setDistrictId(req.districtId);
            a.setWardCode(req.wardCode);
            a.setContactPhone(req.contactPhone);
            a.setPrimaryAddress(req.primaryAddress);
            return addressRepository.save(a);
        });
    }

    @Transactional
    public Optional<Address> updateForUser(Long userId, Long addressId, AddressRequestDTO req) {
        resolveNamesIfNeeded(req);

        return addressRepository.findByIdAndUserId(addressId, userId).map(a -> {
            if (req.primaryAddress && !a.isPrimaryAddress()) {
                addressRepository.findByUserIdAndPrimaryAddressTrue(userId)
                        .ifPresent(prev -> { if (!prev.getId().equals(a.getId())) { prev.setPrimaryAddress(false); addressRepository.save(prev); } });
            }
            if (req.label != null) a.setLabel(req.label);
            if (req.fullAddress != null) a.setFullAddress(req.fullAddress);
            if (req.provinceId != null) a.setProvinceId(req.provinceId);
            if (req.districtId != null) a.setDistrictId(req.districtId);
            if (req.wardCode != null) a.setWardCode(req.wardCode);
            if (req.contactPhone != null) a.setContactPhone(req.contactPhone);
            a.setPrimaryAddress(req.primaryAddress);
            return addressRepository.save(a);
        });
    }

    @Transactional
    public boolean deleteForUser(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId).map(a -> {
            addressRepository.delete(a);
            return true;
        }).orElse(false);
    }

    @Transactional
    public Optional<Address> markPrimary(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId).map(a -> {
            addressRepository.findByUserIdAndPrimaryAddressTrue(userId)
                    .ifPresent(prev -> { if (!prev.getId().equals(a.getId())) { prev.setPrimaryAddress(false); addressRepository.save(prev); } });
            a.setPrimaryAddress(true);
            return addressRepository.save(a);
        });
    }
}