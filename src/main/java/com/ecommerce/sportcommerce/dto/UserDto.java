package com.ecommerce.sportcommerce.dto;

import java.util.UUID;

public record UserDto(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String role,
    String phone,
    String shopName,
    String shopAddress,
    String taxId,
    String emailVerificationStatus,
    String provider
) {}
