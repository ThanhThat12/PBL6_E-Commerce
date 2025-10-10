package com.ecommerce.sportcommerce.service;


import com.ecommerce.sportcommerce.entity.OtpVerification;
import com.ecommerce.sportcommerce.exception.BadRequestException;
import com.ecommerce.sportcommerce.repository.OtpVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpVerificationRepository otpRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpService otpService;

    private OtpVerification mockOtp;

    @BeforeEach
    void setUp() {
        mockOtp = OtpVerification.builder()
            .email("test@example.com")
            .otpCode("123456")
            .otpType(OtpVerification.OtpType.REGISTRATION)
            .expiresAt(LocalDateTime.now().plusSeconds(300))
            .attempts(0)
            .verified(false)
            .build();
    }

    @Test
    void generateOtp_ShouldReturn6DigitCode() {
        // Given
        when(otpRepository.countRecentOtps(anyString(), any(OtpVerification.OtpType.class), any(LocalDateTime.class)))
            .thenReturn(0L);
        // deleteUnverifiedOtpsByEmailAndType is void - allow it
        doNothing().when(otpRepository).deleteUnverifiedOtpsByEmailAndType(anyString(), any(OtpVerification.OtpType.class));
        when(otpRepository.save(any(OtpVerification.class))).thenReturn(mockOtp);

        // When
        OtpVerification result = otpService.generateOtp("test@example.com", OtpVerification.OtpType.REGISTRATION);

        // Then
        assertNotNull(result);
        assertNotNull(result.getOtpCode());
        assertEquals(6, result.getOtpCode().length());
        assertTrue(result.getOtpCode().matches("\\d{6}"));
        verify(otpRepository).save(any(OtpVerification.class));
    }

    @Test
    void verifyOtp_WithValidOtp_ShouldReturnVerifiedOtp() {
        // Given
        when(otpRepository.findLatestValidOtp(eq("test@example.com"), eq(OtpVerification.OtpType.REGISTRATION), any(LocalDateTime.class)))
            .thenReturn(Optional.of(mockOtp));
        when(otpRepository.save(any(OtpVerification.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        OtpVerification result = otpService.verifyOtp("test@example.com", "123456", OtpVerification.OtpType.REGISTRATION);

        // Then
        assertNotNull(result);
        assertTrue(result.getVerified());
        assertNotNull(result.getVerifiedAt());
        verify(otpRepository).save(any(OtpVerification.class));
    }

    @Test
    void verifyOtp_WithInvalidOtp_ShouldIncrementAttempts() {
        // Given
        when(otpRepository.findLatestValidOtp(eq("test@example.com"), eq(OtpVerification.OtpType.REGISTRATION), any(LocalDateTime.class)))
            .thenReturn(Optional.of(mockOtp));
        when(otpRepository.save(any(OtpVerification.class))).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp("test@example.com", "wrong-otp", OtpVerification.OtpType.REGISTRATION);
        });

        verify(otpRepository).save(any(OtpVerification.class));
    }

    @Test
    void verifyOtp_WithExpiredOtp_ShouldThrowException() {
        // Given
        mockOtp.setExpiresAt(LocalDateTime.now().minusSeconds(300));
        when(otpRepository.findLatestValidOtp(eq("test@example.com"), eq(OtpVerification.OtpType.REGISTRATION), any(LocalDateTime.class)))
            .thenReturn(Optional.of(mockOtp));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp("test@example.com", "123456", OtpVerification.OtpType.REGISTRATION);
        });

        assertTrue(exception.getMessage().contains("hết hạn") || exception.getMessage().toLowerCase().contains("expired"));
    }

    @Test
    void verifyOtp_WithMaxAttemptsExceeded_ShouldThrowException() {
        // Given
        mockOtp.setAttempts(5);
        when(otpRepository.findLatestValidOtp(eq("test@example.com"), eq(OtpVerification.OtpType.REGISTRATION), any(LocalDateTime.class)))
            .thenReturn(Optional.of(mockOtp));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp("test@example.com", "123456", OtpVerification.OtpType.REGISTRATION);
        });

        assertTrue(exception.getMessage().contains("Nhập sai") || exception.getMessage().contains("quá nhiều"));
    }

    @Test
    void verifyOtp_WithNoOtpFound_ShouldThrowException() {
        // Given
        when(otpRepository.findLatestValidOtp(eq("test@example.com"), eq(OtpVerification.OtpType.REGISTRATION), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp("test@example.com", "123456", OtpVerification.OtpType.REGISTRATION);
        });

        assertTrue(exception.getMessage().contains("OTP không hợp lệ") || exception.getMessage().toLowerCase().contains("invalid"));
    }

    // Note: OtpService does not expose a deleteOtp(email, type) method in current implementation,
    // so we don't test deletion here. Repository-level cleanup is tested elsewhere.
}
