package com.ecommerce.sportcommerce.dto;

import java.time.Instant;

public record OtpResponse(
    boolean success,
    String message,
    int expiresIn,
    Instant canResendAt
) {}
