backend/
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── com/
│ │ │ └── ecommerce/
│ │ │ ├── EcommerceApplication.java
│ │ │ │
│ │ │ ├── config/
│ │ │ │ ├── SecurityConfig.java # Spring Security configuration
│ │ │ │ ├── JwtConfig.java # JWT configuration
│ │ │ │ ├── EmailConfig.java # JavaMailSender configuration
│ │ │ │ ├── OAuth2Config.java # OAuth2 Google/Facebook config
│ │ │ │ └── CorsConfig.java # CORS configuration
│ │ │ │
│ │ │ ├── entity/
│ │ │ │ ├── User.java # User entity
│ │ │ │ ├── OtpVerification.java # OTP entity
│ │ │ │ └── RefreshToken.java # Refresh token entity
│ │ │ │
│ │ │ ├── repository/
│ │ │ │ ├── UserRepository.java
│ │ │ │ ├── OtpVerificationRepository.java
│ │ │ │ └── RefreshTokenRepository.java
│ │ │ │
│ │ │ ├── dto/
│ │ │ │ ├── request/
│ │ │ │ │ ├── RegisterRequest.java
│ │ │ │ │ ├── VerifyOtpRequest.java
│ │ │ │ │ ├── LoginRequest.java
│ │ │ │ │ ├── ForgotPasswordRequest.java
│ │ │ │ │ ├── ResetPasswordRequest.java
│ │ │ │ │ └── RefreshTokenRequest.java
│ │ │ │ │
│ │ │ │ └── response/
│ │ │ │ ├── ApiResponse.java # Generic API response wrapper
│ │ │ │ ├── AuthResponse.java # Authentication response
│ │ │ │ ├── UserResponse.java # User info response
│ │ │ │ └── ErrorResponse.java # Error response
│ │ │ │
│ │ │ ├── controller/
│ │ │ │ └── AuthController.java # Authentication REST API
│ │ │ │
│ │ │ ├── service/
│ │ │ │ ├── AuthService.java # Authentication business logic
│ │ │ │ ├── OtpService.java # OTP generation & verification
│ │ │ │ ├── EmailService.java # Email sending service
│ │ │ │ ├── JwtService.java # JWT token service
│ │ │ │ ├── RefreshTokenService.java # Refresh token service
│ │ │ │ └── OAuth2Service.java # OAuth2 service
│ │ │ │
│ │ │ ├── security/
│ │ │ │ ├── JwtAuthenticationFilter.java # JWT filter
│ │ │ │ ├── JwtAuthenticationEntryPoint.java
│ │ │ │ ├── CustomUserDetailsService.java
│ │ │ │ ├── UserPrincipal.java # Custom UserDetails
│ │ │ │ └── oauth2/
│ │ │ │ ├── CustomOAuth2UserService.java
│ │ │ │ ├── OAuth2AuthenticationSuccessHandler.java
│ │ │ │ └── OAuth2AuthenticationFailureHandler.java
│ │ │ │
│ │ │ ├── exception/
│ │ │ │ ├── GlobalExceptionHandler.java # @ControllerAdvice
│ │ │ │ ├── ResourceNotFoundException.java
│ │ │ │ ├── BadRequestException.java
│ │ │ │ ├── UnauthorizedException.java
│ │ │ │ └── EmailAlreadyExistsException.java
│ │ │ │
│ │ │ ├── util/
│ │ │ │ ├── OtpGenerator.java # Generate OTP code
│ │ │ │ ├── PasswordValidator.java # Password strength validation
│ │ │ │ └── CookieUtils.java # Cookie helper
│ │ │ │
│ │ │ └── constant/
│ │ │ ├── AppConstants.java # Application constants
│ │ │ └── EmailTemplate.java # Email templates
│ │ │
│ │ └── resources/
│ │ ├── application.yml # Main configuration
│ │ ├── application-dev.yml # Development config
│ │ ├── application-prod.yml # Production config
│ │ └── templates/
│ │ ├── otp-email.html # OTP email template
│ │ └── password-reset-email.html # Password reset email
│ │
│ └── test/
│ └── java/
│ └── com/
│ └── ecommerce/
│ ├── service/
│ │ ├── AuthServiceTest.java
│ │ └── OtpServiceTest.java
│ └── controller/
│ └── AuthControllerTest.java
│
├── pom.xml # Maven dependencies
└── .env # Environment variables (gitignored)
