package com.ecommerce.sportcommerce.dto;

public record AuthResponse(
    boolean success,
    String accessToken,
    String tokenType,
    int expiresIn,
    UserDto user
) {}
